<div>
    <span>
        ${msg("net.ihe.gazelle.gum.dear")} ${firstname} ${lastname} (${organizationName}),
    </span>
    <br/>
    <br/>
    ${msg("net.ihe.gazelle.gum.thanksRegisteredToGazelle")}.
    <br/>
    <br/>
    ${msg("net.ihe.gazelle.gum.linkToActivateYourAccount")} :
    <br/>
    <a href="${activationUrl}">${activationUrl}</a>
    <br/>
    <br/>
    <strong>${msg("net.ihe.gazelle.gum.importantNotice")}:</strong> ${msg("net.ihe.gazelle.gum.ifNotLoginBefore")}
    <strong>${activationLimit}</strong>, ${msg("net.ihe.gazelle.gum.userPurgeMessage")}.
    <span>
        <br/>
        <br/>
        ${msg("net.ihe.gazelle.gum.bestRegards")},
        <br/>
        ${msg("net.ihe.gazelle.gum.gazelleTeam")}
    </span>
</div>