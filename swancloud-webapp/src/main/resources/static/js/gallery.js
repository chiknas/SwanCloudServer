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

// How many items to load per request
const limit = 100;
// Skip so many items to load the next page
let offset = 0;

// Elements
const galleryContainer = document.getElementById("gallery-container");
const beforeDate = document.getElementById("beforeDate");
beforeDate.valueAsDate = new Date();
const filesInput = document.getElementById("files");

// On date filter change refresh the gallery to get results based on the new date
beforeDate.addEventListener("change", (e) => {
  throttle(() => {
    offset = 0;
    galleryContainer.replaceChildren();
    loadNextPage();
  }, 2000);
});

/**
 * Adds a thumbnail of the file with the given id to the gallery
 * @param {*} id the id of the image as returned from the server
 */
const addImage = (id) => {
  const imageContainer = document.createElement("li");

  const img = document.createElement("img");
  img.src = `/api/files/thumbnail/${id}`;
  img.classList.add("galleryImage");
  img.onclick = () => {
    window.location.href = `/preview/${id}`;
  };

  imageContainer.appendChild(img);
  galleryContainer.appendChild(imageContainer);
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
      json?.forEach((element) => addImage(element["id"]));
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