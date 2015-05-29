// use these variables to define the on and off images
// would like to use relative names, but this script is being used from
// multiple directories.  Using full names was the easiest compromise.
var showingImage = "http://aosd.net/2007/img/toggleMinus.gif";
var hiddenImage = "http://aosd.net/2007/img/togglePlus.gif";


//function to display or hide a given element
function showHideItems(myItemName, myButtonName) {
  //this is the ID of the hidden item
  var myItem = document.getElementById(myItemName);

  //this is the ID of the plus/minus button image
  var myButton = document.getElementById(myButtonName);
  if (myItem.style.display != "none") {
      //items are currently displayed, so hide them
      myItem.style.display = "none";
      swapImage(myButton,"plus");
  } else {
      //items are currently hidden, so display them
      myItem.style.display = "block";
      swapImage(myButton,"minus");
  }
}

//function to swap an image based on its current state
function swapImage(myImage, state) {
  if (state == "minus") {
    myImage.src = showingImage;
  } else {
    myImage.src = hiddenImage;
  }
}