const csrfField = document.getElementsByName("_csrf")[0];
const oldPasswordField = document.getElementById("old-password");
const newPasswordField = document.getElementById("new-password");
const repeatPasswordField = document.getElementById("repeat-password");
const submitButton = document.getElementById("submitButton");

const triggerSubmit = (event) => {
  if (event.keyCode === 13) {
    event.preventDefault();
    submitButton.click();
  }
};

// Login on enter press
oldPasswordField.addEventListener("keypress", triggerSubmit);
newPasswordField.addEventListener("keypress", triggerSubmit);
repeatPasswordField.addEventListener("keypress", triggerSubmit);

function resetPassword() {
  const loginRequest = {
    _csrf: csrfField.value,
    oldPassword: oldPasswordField.value,
    newPassword: newPasswordField.value,
  };

  const formBody = Object.keys(loginRequest)
    .map(
      (key) =>
        encodeURIComponent(key) + "=" + encodeURIComponent(loginRequest[key])
    )
    .join("&");

  fetch("/auth/resetpassword", {
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
