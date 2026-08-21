# /cdn/ directory: required server-side change + permission model

## What's broken

Every `/cdn/**` request 403s in production — confirmed live, for every photo
and every story cover image, including the bare `/cdn/` root itself. `/static/`
(a different nginx `alias`, not under a home directory) serves fine on the
same host. That contrast is the signature of a **directory traversal**
problem, not a missing-file problem: nginx's worker process (typically the
`www-data` system user) can't even reach the target directory, regardless of
what's inside it or how those files themselves are permissioned.

Root cause: `nginx/vaadin-og.conf`'s `/cdn/` location aliased to
`/home/tasos/downloads/docker/photoact-cdn/` — the real host-side path of the
`photoact` container's bind-mounted CDN volume, confirmed correct against
`docker-compose.yml` by a previous session. The problem isn't that path being
*wrong*; it's that it lives **under a user's home directory**. Ubuntu home
directories default to mode `750` or `700` — traversable only by their owner
(and root) — which is precisely what a home directory is *for*. Using one to
hold assets a different system user (nginx) must read fights that default
rather than working with it, and reintroduces the same failure mode on any
future `useradd`/home-directory reset, disk reorg, or fresh server build.

## The fix: two parts

### 1. nginx config (this PR)

`location /cdn/` now aliases to `/var/www/photoact/cdn/` — the same
container-side path already assumed by `app.cdn.root`'s default in
`application.properties`, and the same pattern `/static/` already uses
successfully. `/var/www/` is a standard, world-traversable system path
(typically `root:root 755`), so this sidesteps the home-directory permission
trap entirely instead of just patching around it.

**This nginx change alone does nothing until the matching host-side change
below is made** — the alias will simply point at an empty/missing directory.
Deploy them together.

### 2. Server-side: move the bind mount (do this on the host)

```bash
# 1. Stop the container so the volume isn't being written to mid-copy.
docker compose stop app3        # or whatever the photoact service is named

# 2. Create the new host path and copy the data across, preserving
#    ownership/permissions/timestamps.
sudo mkdir -p /var/www/photoact/cdn
sudo rsync -a --info=progress2 /home/tasos/downloads/docker/photoact-cdn/ /var/www/photoact/cdn/

# 3. Update docker-compose.yml's volume line for app3 from:
#      - /home/tasos/downloads/docker/photoact-cdn:/var/www/photoact/cdn
#    to:
#      - /var/www/photoact/cdn:/var/www/photoact/cdn

# 4. Fix ownership/permissions on the new tree (see model below), then:
docker compose up -d app3

# 5. Reload nginx to pick up this PR's alias change:
sudo nginx -t && sudo systemctl reload nginx

# 6. Verify, then remove the old copy once you're confident:
curl -I https://photoact.net/cdn/
#   ... spot-check a couple of real og/ and thumb/ URLs from
#   /og/raw/{type}/{slug} responses too ...
sudo rm -rf /home/tasos/downloads/docker/photoact-cdn
```

If a full migration isn't feasible right now, the **minimal stopgap** is to
leave the alias/bind-mount where they are and just open traversal on the
home-directory chain:

```bash
chmod o+x /home/tasos /home/tasos/downloads /home/tasos/downloads/docker
```

`+x` alone (without `+r`) lets nginx traverse *through* those directories
without making them listable — `/home/tasos` itself doesn't become
browsable, only passable. This fixes the immediate 403s but leaves the
underlying fragility (home dir holding public web assets) in place; prefer
the full migration above when there's a maintenance window for it.

## Permission model for the /cdn/ tree (wherever it ends up living)

These files are all meant to be public — no sensitivity concern — so the
policy below optimizes for "nginx can always read, only the writer can
change," not secrecy.

| Path type | Mode | Why |
|---|---|---|
| Every ancestor directory (`/var`, `/var/www`, `/var/www/photoact`, `/var/www/photoact/cdn`) | at least `o+x` (traverse) | nginx needs to pass through every directory in the chain, not just the final one. Standard system paths like `/var/www` already have this by default — that's exactly why this location is safe to reuse. |
| `/cdn/` subdirectories (`og/`, `thumb/`, `original/`) | `755` (`rwxr-xr-x`) | Owner (the account/UID the app or CDN-processing script writes as) gets full access; everyone else — including nginx — gets read + traverse, never write. |
| Image files themselves | `644` (`rw-r--r--`) | Owner can read/write; everyone else read-only. No execute bit — they're images, not scripts. |
| `original/` specifically | filesystem perms same as above | Public read access is already blocked at the nginx layer (`location ~* ^/cdn/original/ { deny all; return 403; }`, unchanged by this PR) — that's the real control. Filesystem perms don't need to differ; nginx still needs to traverse *into* `/cdn/` to reach `og/`/`thumb/` even though `original/` itself is walled off by config. |

**Ownership**: set the tree's owner to whichever account actually writes new
CDN variants (the container's app user, or whoever runs the reprocessing
script named by `app.cdn.python`/`app.cdn.script` in
`application.properties`). Ownership doesn't need to match nginx's user at
all — the `o+rx` (dirs) / `o+r` (files) bits above are exactly what let a
*different* user (nginx) read without needing shared group membership. Add a
shared group instead only if you'd rather not rely on world-readable bits.

**Apply uniformly**, don't hand-tune individual files:

```bash
sudo find /var/www/photoact/cdn -type d -exec chmod 755 {} \;
sudo find /var/www/photoact/cdn -type f -exec chmod 644 {} \;
```

**Never** use `777` anywhere in this chain — it's never required by the
above (execute-only for traversal, read+execute for public directories, read
for public files always suffice) and it grants write access to every user on
the box, including the nginx/www-data account that has no legitimate reason
to modify these files.

**If permissions look correct and it still 403s**: check for an AppArmor
profile restricting nginx's readable paths (`aa-status`, and grep
`/var/log/syslog` or `/var/log/kern.log` for `DENIED` lines mentioning
`nginx` around the time of a failing request). Not common on stock Ubuntu
nginx packages, but worth ruling out before re-litigating file permissions
that are already correct.

## Verifying the fix

```bash
# Should be 200 with a real image, not 403/HTML:
curl -I https://photoact.net/cdn/og/<some-real-filename>.jpg

# Confirm via the app's own OG endpoint that a real photo/story now has a
# loadable image end-to-end:
curl -s -A "facebookexternalhit/1.1" \
  https://photoact.net/og/raw/story/<some-slug> | python3 -m json.tool
# ... then curl -I the "ogImage" value from that response directly.
```

Re-run Facebook's Sharing Debugger (or any other link-preview tool) with
"Scrape Again" afterward — it caches its own last scrape and won't refetch
on its own.
