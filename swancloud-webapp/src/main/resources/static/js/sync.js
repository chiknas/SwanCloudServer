function generateQrCode() {
  const sideNav = document.getElementById("mySidenav");
  const lastUpload = document.getElementById("last-upload");
  const qrcode =
    document.getElementById("qrcode") ?? document.createElement("img");

  qrcode.classList.add("sidenav-item");
  qrcode.id = "qrcode";
  qrcode.src = qrcode.src ?? "/img/icons/loading.gif";

  sideNav.insertBefore(qrcode, lastUpload);

  fetch(`/api/syncqr`)
    .then((res) => res.blob())
    .then((response) => {
      qrcode.src = URL.createObjectURL(response);
    });
}
