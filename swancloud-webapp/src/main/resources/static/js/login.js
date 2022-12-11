const csrfField = document.getElementsByName("_csrf")[0];
const usernameField = document.getElementById("username");
const passwordField = document.getElementById("password");
const submitButton = document.getElementById("submitButton");

const triggerSubmit = (event) => {
  if (event.keyCode === 13) {
    event.preventDefault();
    submitButton.click();
  }
};

// Login on enter press
usernameField.addEventListener("keypress", triggerSubmit);
passwordField.addEventListener("keypress", triggerSubmit);

function login() {
  const loginRequest = {
    _csrf: csrfField.value,
    username: usernameField.value,
    password: passwordField.value,
  };

  const formBody = Object.keys(loginRequest)
    .map(
      (key) =>
        encodeURIComponent(key) + "=" + encodeURIComponent(loginRequest[key])
    )
    .join("&");

  fetch("/login", {
    method: "POST",
    body: formBody,
    credentials: "same-origin",
    headers: {
      "content-type": "application/x-www-form-urlencoded",
    },
  }).then((response) => {
    if (response.redirected) {
      window.location.href = response.url;
    }
  });
}
