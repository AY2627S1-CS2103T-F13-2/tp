// Keep every feature readable when JavaScript is unavailable.
(() => {
  const page = document.querySelector('.landing-page');
  if (!page) {
    return;
  }

  const selector = page.querySelector('.landing-feature-selector');
  const buttons = [...selector.querySelectorAll('button')];
  const features = buttons.map(button => document.getElementById(button.getAttribute('aria-controls')));
  if (features.some(feature => !feature)) {
    return;
  }

  function selectFeature(selectedIndex) {
    buttons.forEach((button, index) => {
      button.setAttribute('aria-pressed', String(index === selectedIndex));
      features[index].hidden = index !== selectedIndex;
    });
  }

  buttons.forEach((button, index) => {
    button.addEventListener('click', () => selectFeature(index));
  });
  page.classList.add('landing-interactive');
  selectFeature(0);
  selector.hidden = false;
})();
