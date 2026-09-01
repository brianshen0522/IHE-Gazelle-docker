<div>
    <span>
        ${msg("net.ihe.gazelle.gum.dear")} ${firstname} ${lastname},
    </span>
    <br>
    <br>
    ${msg("net.ihe.gazelle.gum.adminCreatedYouAnAccount")}.
    <br>
    <br>
    ${msg("net.ihe.gazelle.gum.accountAssociatedToEmail")} ${email}.
    <br>
    <br>
    ${msg("net.ihe.gazelle.gum.accountActivatedButNoPassword")} :
    <br>
    <a href="${resetPasswordUrl}">${resetPasswordUrl}</a>
    <br>
    ${msg("net.ihe.gazelle.gum.ifNotLoginBefore")} ${activationLimit},
    ${msg("net.ihe.gazelle.gum.userPurgeMessage")}.
    <br>
    <br>
    <span>
        ${msg("net.ihe.gazelle.gum.bestRegards")},
        <br>
        ${msg("net.ihe.gazelle.gum.gazelleTeam")}
    </span>
</div>