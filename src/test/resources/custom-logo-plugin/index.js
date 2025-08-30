// =============================
// Custom Allure Report JS Plugin
// =============================

document.addEventListener("DOMContentLoaded", function () {
    applyCustomLogo();
    addCustomFooter();
    checkForCelebration();
});

function applyCustomLogo() {
    const logoUrl = window.allureConfig?.logoBase64;
    if (!logoUrl) return;
    const brandEl = document.querySelector(".side-nav__brand");
    if (brandEl) {
        brandEl.style.background = `url("${logoUrl}") no-repeat left center`;
        brandEl.style.backgroundSize = "contain";
        brandEl.style.marginLeft = "5px";
        brandEl.style.height = "40px";
        // Hide inner text
        const span = brandEl.querySelector("span");
        if (span) {
            span.style.display = "none";
        }
    }
}


// ---------- Footer ----------
function addCustomFooter() {
    if (document.querySelector("#custom-footer")) return;
    const footer = document.createElement("div");
    footer.id = "custom-footer";
    const year = new Date().getFullYear();
    footer.innerHTML = `✨ Allure Report ✨ © ${year} | QA-Shivam`;
    document.body.appendChild(footer);
}

// ---------- Confetti 🎉 ----------
function launchConfetti() {
    for (let i = 0; i < 80; i++) {
        const confetto = document.createElement("div");
        confetto.classList.add("confetto");
        document.body.appendChild(confetto);

        confetto.style.left = Math.random() * 100 + "vw";
        confetto.style.animationDuration = (Math.random() * 3 + 2) + "s";
        confetto.style.backgroundColor = `hsl(${Math.random() * 360}, 100%, 50%)`;
        confetto.style.transform = `rotate(${Math.random() * 360}deg)`;

        setTimeout(() => confetto.remove(), 5000);
    }
}

// ---------- Fireworks 🎆 ----------
function launchFireworks() {
    for (let i = 0; i < 6; i++) {
        const firework = document.createElement("div");
        firework.classList.add("firework");
        document.body.appendChild(firework);

        // Random position
        firework.style.left = Math.random() * 100 + "vw";
        firework.style.top = Math.random() * 50 + "vh";

        setTimeout(() => firework.remove(), 2000);
    }
}

// ---------- Celebration Logic 🎊 ----------
function checkForCelebration() {
    setTimeout(function () {
        let passRate = 0;
        const captionEl = document.querySelector("text.chart__caption");
        if (captionEl) {
            const text = captionEl.textContent.trim();
            const match = text.match(/(\d+\.?\d*)%/);
            if (match) {
                passRate = parseFloat(match[1]);
            }
        }
        if (passRate >= 60) {
            startCelebration(passRate);
        }
    }, 500);
}

function startCelebration(passRate) {
    launchConfetti();
    if (passRate === 100) {
        launchFireworks();
    }
    showCelebrationBanner(passRate);
}

// ---------- Banner 🎇 ----------
function showCelebrationBanner(passRate) {
    if (document.querySelector("#celebration-banner")) return;
    const banner = document.createElement("div");
    banner.id = "celebration-banner";
    let message = "";
    if (passRate === 100) {
        message = '🎉 PERFECT! 100% TESTS PASSED! 🎉';
    } else if (passRate >= 90) {
        message = '🌟 EXCELLENT! ' + passRate + '% TESTS PASSED! 🌟';
    } else if (passRate >= 80) {
        message = '👏 GREAT JOB! ' + passRate + '% TESTS PASSED! 👏';
    } else {
        message = '✨ GOOD WORK! ' + passRate + '% TESTS PASSED! ✨';
    }
    banner.innerHTML = message;
    document.body.appendChild(banner);
    setTimeout(() => banner.remove(), 5000);
}

