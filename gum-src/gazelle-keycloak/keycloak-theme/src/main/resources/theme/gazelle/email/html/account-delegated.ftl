<#import "template.ftl" as layout>
<@layout.emailLayout>
    <div>
        <span>
            ${kcSanitize(msg("net.ihe.gazelle.gum.dear", firstName, lastName))?no_esc}
            <br>
        </span>
        <br>
        ${msg("net.ihe.gazelle.gum.messageFromTestBedUrl")}
        <a href="${kcSanitize(testBedUrl)?no_esc}">${kcSanitize(testBedUrl)?no_esc}</a>
        <br>
        <br>
        ${msg("net.ihe.gazelle.gum.delegatedAccount", email, idp)}
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