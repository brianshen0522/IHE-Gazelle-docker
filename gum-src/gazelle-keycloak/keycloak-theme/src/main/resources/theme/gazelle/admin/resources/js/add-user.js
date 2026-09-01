/**
  * This script is used to update the user creation page
  * @author Valentin Lorand
  * @date 2023-10-13
*/

// Add a listener on click event
document.addEventListener("click", () => {
  // Check that the path ends with /add-user
  if (window.location.href.endsWith('/gazelle/users/add-user')) {
      performUpdates();
      updateButtonState();
  }
});

document.onreadystatechange = function () {
    if (window.location.href.endsWith('/gazelle/users/add-user')) {
        if (document.readyState == "complete") {
              wait(1000);
              performUpdates();
              updateButtonState();
        }
    }
}

function performUpdates() {
    waitForElm('.pf-c-form__group').then((elm) => {
        // select fields
        const username = document.getElementById('username');
        const emailVerified = document.getElementsByClassName('pf-c-form__group')[3];
        const joinGroup = document.getElementById('kc-join-groups-button');
        const title = document.getElementsByClassName('kc-username-view-header')[0];

        // Update title
        title.innerHTML = 'Create a Gazelle user';

        // Set random values for username
        if(username.value === '') {
           username.select();
            username.value = Math.random().toString(36).substring(7);
        }

        // Rename groups to organizations
        joinGroup.parentNode.parentNode.parentNode.getElementsByTagName('span')[0].innerHTML = 'Organizations';
        joinGroup.innerHTML = 'Join organizations';

        // Hide the username input and the email verified checkbox
        username.parentNode.style.display = 'none';
        emailVerified.style.display = 'none';
    });
}

function updateButtonState() {
  waitForElm(".pf-c-form__group-control").then((elm) => {
    const email = document.getElementById("email");
    const firstname = document.getElementById("firstName");
    const lastname = document.getElementById("lastName");
    const createButton = document.getElementsByClassName("pf-c-button pf-m-primary")[0];

    // Add listeners on the create button
    firstname.addEventListener("input", updateButtonState);
    lastname.addEventListener("input", updateButtonState);
    email.addEventListener("input", updateButtonState);

    const isAnyFieldEmpty = !email.value || !firstname.value || !lastname.value;

    if (isAnyFieldEmpty) {
      createButton.setAttribute("disabled", true);
      createButton.setAttribute("aria-disabled", "true");
      if (!createButton.classList.contains("pf-m-disabled")) {
        createButton.classList.add("pf-m-disabled");
      }
    } else {
      createButton.removeAttribute("disabled");
      createButton.removeAttribute("aria-disabled");
      createButton.classList.remove("pf-m-disabled");
    }
  });
}

function waitForElm(selector) {
  return new Promise((resolve) => {
    if (document.querySelector(selector)) {
      return resolve(document.querySelector(selector));
    }

    const observer = new MutationObserver((mutations) => {
      if (document.querySelector(selector)) {
        observer.disconnect();
        resolve(document.querySelector(selector));
      }
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true,
    });
  });
}

function wait(milliseconds) {
  const date = Date.now();
  let currentDate = null;
  do {
    currentDate = Date.now();
  } while (currentDate - date < milliseconds);
}