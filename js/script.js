// Initialization function called on page load
function initGallery() {
  console.log("Page loaded: initializing gallery");
  addTabIndexToThumbnails();
}

// Automatically add tabindex to each thumbnail image for keyboard navigation
function addTabIndexToThumbnails() {
  const thumbnails = document.querySelectorAll("#gallery img");
  for (let i = 0; i < thumbnails.length; i++) {
    thumbnails[i].setAttribute("tabindex", "0");
    console.log("Added tabindex to thumbnail " + (i + 1));
  }
}

// Event handler for mouseover and focus events
function handleMouseOver(imgElement) {
  console.log("Mouse over on image: " + imgElement.alt);
  updateLargeImage(imgElement);
}

function handleFocus(imgElement) {
  console.log("Image focused: " + imgElement.alt);
  updateLargeImage(imgElement);
}

// Event handler for mouseleave and blur events
function handleMouseLeave() {
  console.log("Mouse left image");
  revertLargeImage();
}

function handleBlur() {
  console.log("Image lost focus");
  revertLargeImage();
}

// Updates the large image display with the thumbnail's image and alt text
function updateLargeImage(imgElement) {
  const largeImageDiv = document.getElementById("largeImage");
  const largeImageText = document.getElementById("largeImageText");
  
  largeImageDiv.setAttribute("aria-label", imgElement.alt);
  largeImageDiv.style.backgroundImage = "url('" + imgElement.src + "')";
  largeImageText.textContent = imgElement.alt;
}

function revertLargeImage() {
  const largeImageDiv = document.getElementById("largeImage");
  const largeImageText = document.getElementById("largeImageText");
  
  largeImageDiv.setAttribute("aria-label", "Large image display");
  largeImageDiv.style.backgroundImage = "";
  largeImageText.textContent = "Original Image";
}
