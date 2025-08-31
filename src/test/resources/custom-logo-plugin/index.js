// =============================
// Custom Allure Report JS Plugin
// =============================

console.log("Custom Allure JS loaded");

function waitForAllureElements() {
    const brandEl = document.querySelector(".side-nav__brand");
    const captionEl = document.querySelector("text.chart__caption");
    if (brandEl && captionEl) {
        applyCustomLogo();
        addCustomFooter();
        checkForCelebration();
    } else {
        setTimeout(waitForAllureElements, 300);
    }
}

waitForAllureElements();

function applyCustomLogo() {
    // You can set your logoBase64 in allureConfig or directly in CSS
    const logoUrl = window.allureConfig?.logoBase64;
    if (!logoUrl) return;
    const brandEl = document.querySelector(".side-nav__brand");
    if (brandEl) {
        brandEl.style.background = `url("${logoUrl}") no-repeat left center`;
        brandEl.style.backgroundSize = "contain";
        brandEl.style.marginLeft = "5px";
        brandEl.style.height = "40px";
        const span = brandEl.querySelector("span");
        if (span) span.style.display = "none";
    }
}

function addCustomFooter() {
    if (document.querySelector("#custom-footer")) return;
    const footer = document.createElement("div");
    footer.id = "custom-footer";
    const year = new Date().getFullYear();
    footer.innerHTML = `✨ Allure Report ✨ © ${year} | QA-Shivam`;
    document.body.appendChild(footer);
}

function launchConfetti() {
    for (let i = 0; i < 80; i++) {
        const confetto = document.createElement("div");
        confetto.classList.add("confetto");
        confetto.style.left = Math.random() * 100 + "vw";
        confetto.style.animationDuration = (Math.random() * 3 + 2) + "s";
        confetto.style.backgroundColor = `hsl(${Math.random() * 360}, 100%, 50%)`;
        confetto.style.transform = `rotate(${Math.random() * 360}deg)`;
        document.body.appendChild(confetto);
        setTimeout(() => confetto.remove(), 5000);
    }
}

function launchFireworks() {
    for (let i = 0; i < 6; i++) {
        const firework = document.createElement("div");
        firework.classList.add("firework");
        firework.style.left = Math.random() * 100 + "vw";
        firework.style.top = Math.random() * 50 + "vh";
        document.body.appendChild(firework);
        setTimeout(() => firework.remove(), 2000);
    }
}

function checkForCelebration() {
    let attempts = 0;
    function poll() {
        const captionEl = document.querySelector("text.chart__caption");
        if (captionEl) {
            const text = captionEl.textContent.trim();
            console.log("captionEl.textContent:", JSON.stringify(text));
            // Direct parse, fallback to regex
            let passRate = 0;
            if (text.endsWith("%")) {
                passRate = parseFloat(text.replace("%", "").trim());
//                console.log("Parsed passRate (endsWith):", passRate);
            } else {
                const match = text.match(/(\d+\.?\d*)/);
                if (match) {
                    passRate = parseFloat(match[1]);
//                    console.log("Parsed passRate (regex):", passRate);
                }
            }
            if (passRate >= 60) {
                startCelebration(passRate);
                return;
            }
        }
        attempts++;
        if (attempts < 50) {
            setTimeout(poll, 200);
        } else {
            console.log("Pass rate not found after polling.");
        }
    }
    poll();
}

function startCelebration(passRate) {
    launchConfetti();
    if (passRate === 100) launchFireworks();
    showCelebrationBanner(passRate);
}

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