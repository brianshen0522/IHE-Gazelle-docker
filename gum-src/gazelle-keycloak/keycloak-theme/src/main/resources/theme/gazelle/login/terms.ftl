<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        ${msg("termsTitle")}
    <#elseif section = "form">
        <div id="kc-terms-text">
            ${kcSanitize(msg("termsText"))?no_esc}
            <br/>
            <br/>
            <a href="${termsOfServiceUrl}" target="_blank" >${msg("termsLink")}</a>
            <br/>
            <a href="${privacyPolicyUrl}" target="_blank" >${msg("privacyLink")}</a>
        </div>
        <form class="form-actions" action="${url.loginAction}" method="POST">
            <div class="kc-login-terms">
                <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="accept" id="kc-accept" type="submit" value="${msg("doAccept")}" />
                <input class="${properties.kcButtonClass!} ${properties.kcButtonLargeClass!}" name="cancel" id="kc-decline" type="submit" value="${msg("doDecline")}" />
            </div>
        </form>
        <div class="clearfix"></div>
    </#if>
</@layout.registrationLayout>