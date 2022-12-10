const csrfField = document.getElementsByName("_csrf")[0];
const usernameField = document.getElementById("username");
const passwordField = document.getElementById("password");

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
    headers: new Headers({
      "content-type": "application/x-www-form-urlencoded",
    }),
  }).then((response) => {
    if (response.redirected) {
      window.location.href = response.url;
    }
  });
}
