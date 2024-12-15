import{s as o,t as e,i}from"../vaadin-dev-tools.js";import{checkboxElement as t,checkedCheckboxElement as a}from"./vaadin-checkbox-4e68df64.js";import"construct-style-sheets-polyfill";import"lit";import"lit/decorators.js";import"lit/directives/class-map.js";import"lit/static-html.js";const r=[e.textColor,e.fontSize,e.fontWeight,e.fontStyle,o.backgroundColor],g={tagName:"vaadin-grid",displayName:"Grid",elements:[{selector:"vaadin-grid",displayName:"Root element",properties:[o.borderColor,o.borderWidth]},{selector:"vaadin-grid::part(header-cell)",displayName:"Header row cell",properties:[e.textColor,{...e.fontSize,propertyName:"--lumo-font-size-s"},e.fontStyle,o.backgroundColor]},{selector:"vaadin-grid::part(body-cell)",displayName:"Body cell",properties:r},{selector:"vaadin-grid::part(even-row-cell)",displayName:"Cell in even row",properties:r},{selector:"vaadin-grid::part(odd-row-cell)",displayName:"Cell in odd row",properties:r},{selector:"vaadin-grid::part(selected-row-cell)",displayName:"Cell in selected row",properties:r},{selector:"vaadin-grid vaadin-grid-cell-content > vaadin-checkbox::part(checkbox)",displayName:"Row selection checkbox",properties:t.properties},{selector:"vaadin-grid vaadin-grid-cell-content > vaadin-checkbox[checked]::part(checkbox)",displayName:"Row selection checkbox (when checked)",properties:a.properties},{selector:"vaadin-grid vaadin-grid-cell-content > vaadin-checkbox::part(checkbox)::after",displayName:"Row selection checkbox checkmark",properties:[i.iconColor]},{selector:"vaadin-grid vaadin-grid-tree-toggle::part(toggle)",displayName:"Expand icon (for tree grid)",properties:[i.iconColor]}]};export{r as cellProperties,g as default};
