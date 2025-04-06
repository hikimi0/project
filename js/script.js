document.querySelector("form").addEventListener("submit", function (e) {
    const email = document.querySelector("input[type='email']").value;
    if (!email.includes("@")) {
      alert("Please enter a valid email address.");
      e.preventDefault();
    }
  });
  