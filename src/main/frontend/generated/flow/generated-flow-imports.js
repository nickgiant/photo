import { injectGlobalCss } from 'Frontend/generated/jar-resources/theme-util.js';

import { css, unsafeCSS, registerStyles } from '@vaadin/vaadin-themable-mixin';
import $cssFromFile_0 from 'Frontend/generated/jar-resources/com/github/appreciated/apexcharts/apexcharts-wrapper-styles.css?inline';
import $cssFromFile_1 from 'Frontend/generated/jar-resources/addons-styles/toggle-button-group.css?inline';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/vertical-layout/theme/lumo/vaadin-vertical-layout.js';
import '@vaadin/dialog/theme/lumo/vaadin-dialog.js';
import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/horizontal-layout/theme/lumo/vaadin-horizontal-layout.js';
import '@vaadin/button/theme/lumo/vaadin-button.js';
import '@vaadin/tooltip/theme/lumo/vaadin-tooltip.js';
import 'Frontend/generated/jar-resources/disableOnClickFunctions.js';
import '@vaadin/icons/vaadin-iconset.js';
import '@vaadin/icon/theme/lumo/vaadin-icon.js';
import '@vaadin/accordion/theme/lumo/vaadin-accordion.js';
import '@vaadin/accordion/theme/lumo/vaadin-accordion-panel.js';
import '@vaadin/details/theme/lumo/vaadin-details.js';
import '@vaadin/scroller/theme/lumo/vaadin-scroller.js';
import '@vaadin/radio-group/theme/lumo/vaadin-radio-group.js';
import 'Frontend/generated/jar-resources/lit-renderer.ts';
import '@vaadin/radio-group/theme/lumo/vaadin-radio-button.js';
import '@vaadin/select/theme/lumo/vaadin-select.js';
import 'Frontend/generated/jar-resources/selectConnector.js';
import 'Frontend/generated/jar-resources/font-awesome-iron-iconset/fas.js';
import '@vaadin/checkbox/theme/lumo/vaadin-checkbox.js';
import '@vaadin/popover/theme/lumo/vaadin-popover.js';
import 'Frontend/generated/jar-resources/vaadin-popover/popover.ts';
import 'Frontend/generated/jar-resources/font-awesome-iron-iconset/far.js';
import 'Frontend/generated/jar-resources/font-awesome-iron-iconset/fab.js';
import '@vaadin/notification/theme/lumo/vaadin-notification.js';
import '@vaadin/context-menu/theme/lumo/vaadin-context-menu.js';
import 'Frontend/generated/jar-resources/contextMenuConnector.js';
import 'Frontend/generated/jar-resources/contextMenuTargetConnector.js';
import '@vaadin/text-area/theme/lumo/vaadin-text-area.js';
import '@vaadin/multi-select-combo-box/theme/lumo/vaadin-multi-select-combo-box.js';
import 'Frontend/generated/jar-resources/comboBoxConnector.js';
import 'Frontend/generated/jar-resources/menubarConnector.js';
import '@vaadin/menu-bar/theme/lumo/vaadin-menu-bar.js';
import '@vaadin/avatar/theme/lumo/vaadin-avatar.js';
import '@vaadin/checkbox-group/theme/lumo/vaadin-checkbox-group.js';
import 'Frontend/generated/jar-resources/paper-slider/fc-l2t-paper-slider.js';
import 'Frontend/generated/jar-resources/com/github/appreciated/apexcharts/apexcharts-wrapper.ts';
import '@vaadin/custom-field/theme/lumo/vaadin-custom-field.js';
import '@vaadin/login/theme/lumo/vaadin-login-form.js';
import '@vaadin/text-field/theme/lumo/vaadin-text-field.js';
import '@vaadin/email-field/theme/lumo/vaadin-email-field.js';
import '@vaadin/password-field/theme/lumo/vaadin-password-field.js';
import '@vaadin/confirm-dialog/theme/lumo/vaadin-confirm-dialog.js';
import '@vaadin/app-layout/theme/lumo/vaadin-app-layout.js';
import '@vaadin/app-layout/theme/lumo/vaadin-drawer-toggle.js';
import '@vaadin/date-picker/theme/lumo/vaadin-date-picker.js';
import 'Frontend/generated/jar-resources/datepickerConnector.js';
import '@vaadin/combo-box/theme/lumo/vaadin-combo-box.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-layout.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-item.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-row.js';
import '@vaadin/tabsheet/theme/lumo/vaadin-tabsheet.js';
import '@vaadin/tabs/theme/lumo/vaadin-tabs.js';
import '@vaadin/tabs/theme/lumo/vaadin-tab.js';
import '@vaadin/upload/theme/lumo/vaadin-upload.js';
import '@vaadin/list-box/theme/lumo/vaadin-list-box.js';
import '@vaadin/item/theme/lumo/vaadin-item.js';
import '@vaadin/grid/theme/lumo/vaadin-grid.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-sorter.js';
import 'Frontend/generated/jar-resources/gridConnector.ts';
import 'Frontend/generated/jar-resources/vaadin-grid-flow-selection-column.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column-group.js';
import '@vaadin/side-nav/theme/lumo/vaadin-side-nav.js';
import '@vaadin/side-nav/theme/lumo/vaadin-side-nav-item.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/color-global.js';
import '@vaadin/vaadin-lumo-styles/typography-global.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';
import 'Frontend/generated/jar-resources/ReactRouterOutletElement.tsx';
const $css_0 = typeof $cssFromFile_0  === 'string' ? unsafeCSS($cssFromFile_0) : $cssFromFile_0;
registerStyles('', $css_0, {moduleId: 'apex-charts-style'});

injectGlobalCss($cssFromFile_1.toString(), 'CSSImport end', document);

const loadOnDemand = (key) => {
  const pending = [];
  if (key === 'a8757d441f9d50c1c3d38b096db88af32253e2a26cea3bad2913b8512ddde16d') {
    pending.push(import('./chunks/chunk-a47944536747ff8219f40a03c4c8ef75d6ea663cf71bcdfc9bb7bb36777c791f.js'));
  }
  if (key === 'c315cff82e6a765aa7f865be8d97b47a1413273fb02a0b4492b351ef1c55aaec') {
    pending.push(import('./chunks/chunk-62636125c711a4404ec7ae9c34808ac79a762bb876bcc7a7cdc9558d0f2482f0.js'));
  }
  if (key === 'd4d42f3c134d1d767aa463e37eea6283ba46545f11159be70d433871589c97e3') {
    pending.push(import('./chunks/chunk-6e61c3a944f52f944060abc65a0d231940d8f5bdbe0d4875b09fab6aec39468e.js'));
  }
  if (key === 'a64213276d43802a82525e0af31a2f468b7be59e0fc81071254ffbdcfe4485c0') {
    pending.push(import('./chunks/chunk-6bbbd86e9faa16dd1841e0c847f35d0c66d4004bffb6550c7a1be66b2dd13156.js'));
  }
  if (key === '0abed118d9f4bea446dc3f1ffb2bc2009e8f0608c13b0905df4284f61af006f1') {
    pending.push(import('./chunks/chunk-1c05d4f854b7e805a1bd2843062a0de5a65afb847f63fd9b1bfdb1f30a25ae90.js'));
  }
  if (key === '343c2ae3e088a09fca216e8e7f1c0ee70d17e3b9962bec52cf79ee62a003cb8f') {
    pending.push(import('./chunks/chunk-08c2f9af60b5466322fdc44e022a8b865d7f114bb8fc1985fd8be348466ff836.js'));
  }
  if (key === 'f19ebcb794289745a8e95193cbed97abaa39094a528f9eba706d095d12d80f94') {
    pending.push(import('./chunks/chunk-9e1616595d2b714a141e85ed5da3c2fdf9dcf5e89c490115ab8794e58b27d1b6.js'));
  }
  if (key === '232f748878068255033be52b7f0ab5ae06fac4edb814a74aa1431c9f5a44c4d6') {
    pending.push(import('./chunks/chunk-f28b7df07b90316537808f4829221677c6e2c9fe5b201a4ac754cd5f1d1ad535.js'));
  }
  if (key === 'c9282b3b100f3439b62697c8164075a2887e7fc7c05145a2657343018c34502a') {
    pending.push(import('./chunks/chunk-78cab6781471d0a6556c2779ca8c38987a6cafe0eced3402239ea913d324eda5.js'));
  }
  if (key === '18677a28cd728117cec19ce5a1093c0fae9b55db541cf684a7543197c6845fbc') {
    pending.push(import('./chunks/chunk-6e61c3a944f52f944060abc65a0d231940d8f5bdbe0d4875b09fab6aec39468e.js'));
  }
  if (key === 'c6cdc93aeaba19a61db07976363dd924318ada001b39870dfdb49b7627f0daaa') {
    pending.push(import('./chunks/chunk-f803df643c07aa37eee57a440b68a91abb3cd3408a72bcc573024c04162131e4.js'));
  }
  if (key === 'ed2008f510cf1513afe42695a8384526a3a4c00b4ac1437ca8cf4b1786b69b56') {
    pending.push(import('./chunks/chunk-62636125c711a4404ec7ae9c34808ac79a762bb876bcc7a7cdc9558d0f2482f0.js'));
  }
  if (key === '3cc76ed38d5b378956aa1962783327c42b8b4a7ca69965689545bf864091dcbf') {
    pending.push(import('./chunks/chunk-678aed121863c3665934f42638b983589a834b34df2895100da32a564c219170.js'));
  }
  if (key === 'fcad8aa5ac490ee1fdb753201dec251c98c686efb1393b91f7b52da0ad0052dc') {
    pending.push(import('./chunks/chunk-b5705885d52676da85ed40d2563510e37fff60edd454726ff8f26552c18ba1c6.js'));
  }
  if (key === 'f9c50ef5bb41e1be3d4010c23ece2b42f838b68aa8221ac19e34094a0f2b02d8') {
    pending.push(import('./chunks/chunk-67f277c4dd3fdd88f6b052814e9b8bc0c45e58f1363c0f054a126fc3d7a86dcc.js'));
  }
  if (key === 'b85ab96eea1bb06e99c6513f47aa9394afcf7e2ba62368abe0c1e7098da06150') {
    pending.push(import('./chunks/chunk-600ffd8fbd1adbf1076e93a4df17620d21ec883c24a627778bfb123455dfc735.js'));
  }
  if (key === 'c8596b045de19cc762891f2d3310dec8b6cef191ec2c9405c6a6ba5c99acadd5') {
    pending.push(import('./chunks/chunk-05c6a86e7948570232a831887ae2e297a062919403da55bf629590c54403cb02.js'));
  }
  if (key === '5530a7a8226808830a79982a7669a70c54e9ce36ed32f0887657011246c6d061') {
    pending.push(import('./chunks/chunk-f5bbacf16d0cebe8e053b965119602d606f33cf350a1981ad3f45aee8ef31759.js'));
  }
  if (key === '6b8da6258f6870370c0f68b7a762e1d1c80e7c8cdff6b599812f2a3733f4d796') {
    pending.push(import('./chunks/chunk-11a7bcebc7fab062a3456e3d30202a2fb88932d900345f70552e44335302e178.js'));
  }
  if (key === '5d923882db60b80e134ecf7510d0157631e197a458e67f0be20a2b4c7ce2d8b5') {
    pending.push(import('./chunks/chunk-54ef2a7509e5d72afb611a7ee048648e7d773fc4d8abce87db8afd4e5acd3e01.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}