const uploadForm = document.getElementById("upload-form");
const filesInput = document.getElementById("files");

// Event listener to upload files as soon as the user selects them.
// No need to click a button or something.
filesInput.addEventListener("change", () => {
  const data = filesInput.files;
  const formData = new FormData();
  for (const name in data) {
    formData.append("files", data[name]);
  }

  fetch("/api/upload", {
    method: "POST",
    body: formData,
  });
});
