<div>
    <span>
    ${msg("net.ihe.gazelle.gum.dear")} ${organizationName} ${msg("net.ihe.gazelle.gum.administrators")},
    </span>
    <br>
    <br>
    ${msg("net.ihe.gazelle.gum.newUserOfYourOrganizationRegister")}.
    ${msg("net.ihe.gazelle.gum.accountIsCurrentlyLocked")}.
    <br>
    <br>
    ${msg("net.ihe.gazelle.gum.findInformationAboutNewUser")}.
    <br>
    * ${msg("net.ihe.gazelle.gum.name")} : ${firstname} ${lastname}
    * ${msg("net.ihe.gazelle.gum.email")} : ${email}
    <br>
    <br>
    ${msg("net.ihe.gazelle.gum.actionToGrantUserAccess")} :
    <br>
    - ${msg("net.ihe.gazelle.gum.eitherClickOnFollowingLink")} : <a href="${activationUrl}">${activationUrl}</a>
    <br>
    - ${msg("net.ihe.gazelle.gum.orActivateUserFromEditionMenu")}
    <br>
    <br>
    ${msg("net.ihe.gazelle.gum.toRejectJustIgnoreThisMail")}.
    ${msg("net.ihe.gazelle.gum.userWillBeDeleted")}: <strong>${activationLimit}</strong>.
    <br>
    <br>
    <span>
    ${msg("net.ihe.gazelle.gum.bestRegards")},
    <br>
    ${msg("net.ihe.gazelle.gum.gazelleTeam")}
    </span>
</div>