<#-- @ftlvariable name="" type="org.gbif.registry.domain.mail.AccountEmailChangedTemplateDataModel" -->
<#include "header.ftl">

<h5 style="margin: 0 0 20px;padding: 0;font-size: 16px;line-height: 1.25;">Вітаємо, {name}!</h5>

<p style="margin: 0 0 20px;padding: 0;line-height: 1.65;">
    Електронна пошта вашого облікового запису GBIF
    <b>${name}</b>
    була змінена на
    <a href="mailto:${newEmail}" style="margin: 0;padding: 0;line-height: 1.65;color: #4ba2ce;text-decoration: none;font-weight: bold;">${newEmail}</a>.
    Якщо ви не змінювали її, будь ласка, зв'яжіться з
    <a href="mailto:helpdesk@gbif.org" style="margin: 0;padding: 0;line-height: 1.65;color: #4ba2ce;text-decoration: none;font-weight: bold;">helpdesk@gbif.org</a>
    негайно.
</p>

<p style="margin: 0 0 20px;padding: 0;line-height: 1.65;">
    <em>Секретаріат GBIF</em>
</p>

<#include "footer.ftl">
