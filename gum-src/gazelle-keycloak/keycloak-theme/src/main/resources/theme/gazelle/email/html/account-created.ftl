<#import "template.ftl" as layout>
<@layout.emailLayout>
    <div>
        <span>
            ${kcSanitize(msg("net.ihe.gazelle.gum.dear", firstName, lastName))?no_esc}
            <br>
        </span>
        <br>
        ${msg("net.ihe.gazelle.gum.messageFromTestBedUrl")} ${testBedUrl}
        <br>
        <br>
        ${kcSanitize(msg("net.ihe.gazelle.gum.accountCreated", organizationName))}
        <br>
        ${msg("net.ihe.gazelle.gum.setPassword")} :
        <br>
        <a href="${kcSanitize(resetPasswordLink)?no_esc}">${kcSanitize(resetPasswordLink)?no_esc}</a>
        <br>
        <br>
        <span>
            ${msg("net.ihe.gazelle.gum.regards")}
            <br>
            ${msg("net.ihe.gazelle.gum.theGazelleTeam")}
            <br>
            ${msg("net.ihe.gazelle.gum.emailNoReply")}
        </span>
    </div>
</@layout.emailLayout>