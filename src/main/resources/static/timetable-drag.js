document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("table.timetable-picker").forEach(function (table) {
        const cells = [...table.querySelectorAll('td:has(input[name="timetable"])')];
        if (!cells.length) return;

        const scope = table.closest("form") || document;
        const modeButton = scope.querySelector("[data-timetable-mode-toggle]");
        const modeLabel = modeButton?.querySelector("[data-mode-label]");
        const modeDescription = scope.querySelector("[data-mode-description]");
        let freeMode = false;
        let dragging = false;
        let dragValue = true;

        function visualSelectionFor(cell) {
            const classTimeSelected = cell.querySelector('input[name="timetable"]').checked;
            return freeMode ? !classTimeSelected : classTimeSelected;
        }

        function renderCell(cell) {
            const selected = visualSelectionFor(cell);
            cell.classList.toggle("timetable-selected", selected);
            cell.setAttribute("aria-checked", String(selected));
        }

        function setVisualSelection(cell, selected) {
            const checkbox = cell.querySelector('input[name="timetable"]');
            const classTimeSelected = freeMode ? !selected : selected;

            if (checkbox.checked !== classTimeSelected) {
                checkbox.checked = classTimeSelected;
                checkbox.dispatchEvent(new Event("change", { bubbles: true }));
            }

            // 기존 change 이벤트가 체크 상태를 표시한 뒤 현재 보기 모드로 다시 렌더링한다.
            renderCell(cell);
        }

        function renderMode() {
            table.classList.toggle("free-time-mode", freeMode);
            cells.forEach(renderCell);

            if (modeButton) {
                modeButton.classList.toggle("is-free-mode", freeMode);
                modeButton.setAttribute("aria-pressed", String(freeMode));
                modeButton.setAttribute("title", freeMode ? "수업시간 보기로 전환" : "공강시간 보기로 전환");
            }
            if (modeLabel) modeLabel.textContent = freeMode ? "공강시간 보기" : "수업시간 보기";
            if (modeDescription) {
                modeDescription.textContent = freeMode
                    ? "공강이 보라색으로 표시됩니다. 저장할 때는 기존 수업시간 형식으로 자동 변환됩니다."
                    : "수업이 있는 시간이 보라색으로 표시됩니다. 셀을 누르거나 드래그해 선택하세요.";
            }
        }

        cells.forEach(function (cell) {
            cell.tabIndex = 0;
            cell.setAttribute("role", "checkbox");
            renderCell(cell);

            cell.addEventListener("pointerdown", function (event) {
                event.preventDefault();
                dragging = true;
                dragValue = !visualSelectionFor(cell);
                setVisualSelection(cell, dragValue);
            });
            cell.addEventListener("pointerenter", function () {
                if (dragging) setVisualSelection(cell, dragValue);
            });
            cell.addEventListener("keydown", function (event) {
                if (event.key === " " || event.key === "Enter") {
                    event.preventDefault();
                    setVisualSelection(cell, !visualSelectionFor(cell));
                }
            });
        });

        table.addEventListener("pointermove", function (event) {
            if (!dragging) return;
            const hit = document.elementFromPoint(event.clientX, event.clientY)?.closest("td");
            if (hit && table.contains(hit) && cells.includes(hit)) setVisualSelection(hit, dragValue);
        });
        window.addEventListener("pointerup", function () { dragging = false; });
        window.addEventListener("pointercancel", function () { dragging = false; });

        modeButton?.addEventListener("click", function () {
            freeMode = !freeMode;
            renderMode();
        });

        renderMode();
    });
});
