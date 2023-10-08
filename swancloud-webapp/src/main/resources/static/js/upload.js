// wakelock.js is required as an import
const filesInput = document.getElementById("files");
const uploadPopup = document.getElementById("upload-popup");
const uploadPopupContent = document.getElementById("upload-popup-content");
const uploadPopupClose = document.getElementById("popup-close");
const uploadPopupTitle = document.getElementById("upload-popup-title");

function enablePopup(enabled) {
  uploadPopup.style.visibility = enabled ? "visible" : "hidden";
  uploadPopup.style.opacity = enabled ? "1" : "0";

  // Reload the page if we are closing this popup
  if (!enabled) {
    location.reload();
  }
}

// Event listener to upload files as soon as the user selects them.
// No need to click a button or something.
filesInput.addEventListener("change", () => {
  enablePopup(true);
  lockWakeState();

  const data = filesInput.files;
  const formData = new FormData();
  for (const name in data) {
    formData.append("files", data[name]);
  }

  fetch("/api/upload", {
    method: "POST",
    body: formData,
  })
    .then(() => {
      const successImage = document.createElement("img");
      successImage.src = "/img/icons/success.gif";
      successImage.className = "popup-gif";
      uploadPopupContent.replaceChildren(successImage);

      uploadPopupTitle.innerText = "Done!";
    })
    .catch(() => {
      const failImage = document.createElement("img");
      failImage.src = "/img/icons/fail.gif";
      failImage.className = "popup-gif";
      uploadPopupContent.replaceChildren(failImage);

      uploadPopupTitle.innerText = "Oops, something went wrong!";
    })
    .finally(() => {
      // Enable popup close button
      uploadPopupClose.style.visibility = "visible";
      uploadPopupClose.style.opacity = "1";
      releaseWakeState();
    });
});
