const pageContainer = document.getElementById("page-container");

const getAccessToken = async () => {
  const refreshTokenResponse = await fetch("/auth/refreshtoken", {
    method: "POST",
  }).then((r) => r.json());

  return refreshTokenResponse.accessToken;
};

const initializeVideoTag = async () => {
  const videoPlayer = createVideoTag();
  videoPlayer.appendChild(await createVideoSourceTag(getVideoId()));

  pageContainer.appendChild(videoPlayer);
};

const getVideoId = () => {
  const path = location.pathname.split("/");
  return path.pop() || path.pop(); // handle potential trailing slash
};

const createVideoTag = () => {
  const videoElement = document.createElement("video");
  videoElement.autoplay = true;
  videoElement.controls = "controls";
  videoElement.classList = ["preview-file"];
  return videoElement;
};

const createVideoSourceTag = async (id) => {
  const sourceElement = document.createElement("source");
  sourceElement.src = `/streaming/files/video/${id}?token=${await getAccessToken()}`;
  return sourceElement;
};

initializeVideoTag();
