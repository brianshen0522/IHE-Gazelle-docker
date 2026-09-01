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
        ${kcSanitize(msg("net.ihe.gazelle.gum.tooManyLogin",email))?no_esc}
        <br>
        ${msg("net.ihe.gazelle.gum.accountTemporarilyLocked")}
        <br>
        ${msg("net.ihe.gazelle.gum.ifYouAreAtTheOrigin")} ${msg("net.ihe.gazelle.gum.resetLink")}
        <a href="${kcSanitize(resetPasswordLink)?no_esc}">${kcSanitize(resetPasswordLink)?no_esc}</a>
        ${msg("net.ihe.gazelle.gum.unlockAccount")}
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