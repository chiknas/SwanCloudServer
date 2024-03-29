// HELPER FUNCTIONS
var throttleTimer;

const throttle = (callback, time) => {
  if (throttleTimer) return;

  throttleTimer = true;

  setTimeout(() => {
    callback();
    throttleTimer = false;
  }, time);
};

function openNav() {
  document.getElementById("mySidenav").style.width = "250px";
  document.getElementById("mySidenav").style["padding-left"] = "1em";
  document.getElementById("mySidenav").style["padding-right"] = "1em";
}

function closeNav() {
  document.getElementById("mySidenav").style.width = "0";
  document.getElementById("mySidenav").style["padding-left"] = "0";
  document.getElementById("mySidenav").style["padding-right"] = "0";
}

function parseDate(date) {
  const dd = date.getDate();
  const mm = date.getMonth() + 1;
  const yyyy = date.getFullYear();

  return `${yyyy}-${mm}-${dd}`;
}

// How many items to load per request
const limit = 100;
// Skip so many items to load the next page
let offset = 0;

// Elements
const galleryContainer = document.getElementById("gallery-container");
const beforeDate = document.getElementById("beforeDate");
beforeDate.value = parseDate(new Date());

// On date filter change refresh the gallery to get results based on the new date
beforeDate.addEventListener("input", (e) => {
  throttle(() => {
    offset = 0;
    galleryContainer.replaceChildren();
    loadNextPage();
  }, 2000);
});

/**
 * Returns a div container with a div child to be used as an overlay
 * div with a background image using css.
 */
const getPlayButtonOverlayContainer = (enabled) => {
  const overlayItem = document.createElement("img");
  overlayItem.classList.add("overlay-img");
  overlayItem.src = "/img/icons/play-button.png";
  if (!enabled) {
    overlayItem.style.opacity = 0;
  }

  const overlayContainer = document.createElement("div");
  overlayContainer.classList.add("overlay-container");
  overlayContainer.appendChild(overlayItem);

  return overlayContainer;
};

/**
 * Adds a thumbnail of the file with the given id to the gallery
 * @param {*} id the id of the image as returned from the server
 */
const addImage = (id, isVideo) => {
  const listItemContainer = document.createElement("li");
  listItemContainer.onclick = () => {
    window.open(`/preview/${id}`, "_blank");
  };

  const img = document.createElement("img");
  img.classList.add("galleryImage");

  const overlayContainer = getPlayButtonOverlayContainer(isVideo);
  overlayContainer.appendChild(img);

  listItemContainer.appendChild(overlayContainer);
  galleryContainer.appendChild(listItemContainer);

  fetch(`/api/files/thumbnail/${id}`)
    .then((res) => res.blob())
    .then((response) => {
      img.src = URL.createObjectURL(response);
    });
};

/**
 * Loads the next set of images and moved the offset forward for the next page to be loaded.
 */
const loadNextPage = () => {
  fetch(
    `/api/files?limit=${limit}&offset=${offset}&beforeDate=${beforeDate.value}`
  )
    .then((response) => response.json())
    .then((json) => {
      json?.forEach((element) => {
        const elementType = element["fileMimeType"];
        const isVideo = elementType.includes("video");
        addImage(element["id"], isVideo);
      });
    })
    .then(() => (offset += limit))
    .catch((e) => console.error(e));
};

// init page by loading some images
loadNextPage();

// INFINITE SCROLL
const handleInfiniteScroll = () => {
  throttle(() => {
    const endOfPage =
      window.innerHeight + window.pageYOffset >=
      document.body.offsetHeight - 200;

    if (endOfPage) {
      loadNextPage();
    }
  }, 1000);
};
window.addEventListener("scroll", handleInfiniteScroll);

// UNCATEGORISED FILTER: Render files on the EPOCH date only.
const uncategorisedButton = document.getElementById("uncategorized-button");
function uncategorised() {
  const isUncategorised = beforeDate.value == "1970-01-01";

  if (isUncategorised) {
    beforeDate.value = parseDate(new Date());
    beforeDate.disabled = false;
    uncategorisedButton.classList.remove("btn-pressed");
  } else {
    beforeDate.value = "1970-01-01";
    beforeDate.disabled = true;
    uncategorisedButton.classList.add("btn-pressed");
  }

  beforeDate.dispatchEvent(new Event("input", { bubbles: true }));
}
