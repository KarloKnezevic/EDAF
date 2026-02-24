/**
 * Shared EDAF dashboard helpers loaded on every web page.
 */
(function () {
    function formatInt(value) {
        if (value === null || value === undefined || value === "") {
            return "-";
        }
        return new Intl.NumberFormat().format(Number(value));
    }

    function formatGb(value) {
        if (value === null || value === undefined || Number.isNaN(Number(value))) {
            return "-";
        }
        return Number(value).toFixed(3) + " GB";
    }

    function fetchHeaderStats() {
        var dbSizeNode = document.getElementById("brandDbSize");
        var expCountNode = document.getElementById("brandExperimentCount");
        var runCountNode = document.getElementById("brandRunCount");
        var runtimeNode = document.getElementById("brandTotalRuntime");
        if (!dbSizeNode || !expCountNode || !runCountNode || !runtimeNode) {
            return;
        }
        fetch("/api/dashboard/summary")
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("HTTP " + response.status);
                }
                return response.json();
            })
            .then(function (summary) {
                dbSizeNode.textContent = formatGb(summary.databaseSizeGb);
                expCountNode.textContent = formatInt(summary.experimentCount);
                runCountNode.textContent = formatInt(summary.runCount);
                runtimeNode.textContent = formatInt(summary.totalRuntimeMillis);
            })
            .catch(function () {
                dbSizeNode.textContent = "-";
                expCountNode.textContent = "-";
                runCountNode.textContent = "-";
                runtimeNode.textContent = "-";
            });
    }

    function initBrand() {
        var header = document.querySelector(".brand-header");
        if (!header) {
            return;
        }
        var onScroll = function () {
            if (window.scrollY > 4) {
                header.style.boxShadow = "0 6px 14px rgba(0,0,0,.08)";
            } else {
                header.style.boxShadow = "none";
            }
        };
        onScroll();
        window.addEventListener("scroll", onScroll, { passive: true });
        fetchHeaderStats();
        window.setInterval(fetchHeaderStats, 10000);
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initBrand);
    } else {
        initBrand();
    }
})();
