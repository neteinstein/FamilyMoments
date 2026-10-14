package org.neteinstein.family.data.local

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsNumber
import kotlin.js.JsString
import kotlin.js.Promise

/**
 * Raw Kotlin/Wasm-JS interop for the "family_moments" IndexedDB database backing
 * [IndexedDbQuestionLocalDataSource]. Each operation below opens its own connection via the shared
 * `globalThis.__fmOpenDb()` helper (installed once, idempotently, by [jsInstallIdbHelper]) and
 * closes it when its transaction completes - IndexedDB has no issue with many short-lived
 * connections, and this avoids managing a connection's lifetime across suspend calls.
 *
 * Rows cross the boundary as flat, delimited strings (ASCII record/unit separators) instead of
 * JSON: `@JsFun` only carries primitives, strings, and [JsAny] values directly, so JSON would still
 * need a hand-written parser on the Kotlin side - a plain `split()` on both sides is simpler for
 * this module's small, fixed row shape (id/text/languageCode/category).
 */
@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    () => {
        if (globalThis.__fmOpenDb) return;
        globalThis.__fmOpenDb = function () {
            return new Promise((resolve, reject) => {
                const request = indexedDB.open('family_moments', 1);
                request.onupgradeneeded = (event) => {
                    const db = event.target.result;
                    if (!db.objectStoreNames.contains('cards')) {
                        db.createObjectStore('cards', { keyPath: 'id' });
                    }
                    if (!db.objectStoreNames.contains('seed_metadata')) {
                        db.createObjectStore('seed_metadata', { keyPath: 'id' });
                    }
                };
                request.onerror = () => reject(request.error);
                request.onsuccess = () => resolve(request.result);
            });
        };
    }
    """,
)
internal external fun jsInstallIdbHelper()

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    () => globalThis.__fmOpenDb().then((db) => new Promise((resolve, reject) => {
        const tx = db.transaction(['seed_metadata'], 'readonly');
        const getReq = tx.objectStore('seed_metadata').get(0);
        tx.oncomplete = () => { db.close(); resolve(getReq.result ? getReq.result.version : null); };
        tx.onerror = () => { db.close(); reject(tx.error); };
    }))
    """,
)
internal external fun idbGetSeedVersion(): Promise<JsNumber?>

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    (cardRows, hiddenIdsCsv, seedVersion) => globalThis.__fmOpenDb().then((db) => new Promise((resolve, reject) => {
        const RS = '\u001e';
        const US = '\u001f';
        const hiddenIds = new Set(hiddenIdsCsv.length ? hiddenIdsCsv.split(',').map(Number) : []);
        const tx = db.transaction(['cards', 'seed_metadata'], 'readwrite');
        const cardsStore = tx.objectStore('cards');
        cardsStore.clear();
        if (cardRows.length) {
            for (const row of cardRows.split(RS)) {
                const fields = row.split(US);
                const numericId = Number(fields[0]);
                cardsStore.put({
                    id: numericId,
                    text: fields[1],
                    languageCode: fields[2],
                    category: fields[3],
                    isHidden: hiddenIds.has(numericId),
                });
            }
        }
        tx.objectStore('seed_metadata').put({ id: 0, version: seedVersion });
        tx.oncomplete = () => { db.close(); resolve(null); };
        tx.onerror = () => { db.close(); reject(tx.error); };
    }))
    """,
)
internal external fun idbReplaceAllCards(
    cardRows: String,
    hiddenIdsCsv: String,
    seedVersion: Int,
): Promise<JsAny?>

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    (languageCode) => globalThis.__fmOpenDb().then((db) => new Promise((resolve, reject) => {
        const tx = db.transaction(['cards'], 'readonly');
        const getAllReq = tx.objectStore('cards').getAll();
        tx.oncomplete = () => {
            db.close();
            const US = '\u001f';
            const RS = '\u001e';
            const rows = getAllReq.result
                .filter((c) => c.languageCode === languageCode)
                .map((c) => [c.id, c.text, c.languageCode, c.category].join(US));
            resolve(rows.join(RS));
        };
        tx.onerror = () => { db.close(); reject(tx.error); };
    }))
    """,
)
internal external fun idbGetQuestionsForLanguage(languageCode: String): Promise<JsString?>

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    () => globalThis.__fmOpenDb().then((db) => new Promise((resolve, reject) => {
        const tx = db.transaction(['cards'], 'readonly');
        const getAllReq = tx.objectStore('cards').getAll();
        tx.oncomplete = () => {
            db.close();
            resolve(getAllReq.result.filter((c) => c.isHidden).map((c) => c.id).join(','));
        };
        tx.onerror = () => { db.close(); reject(tx.error); };
    }))
    """,
)
internal external fun idbGetHiddenIds(): Promise<JsString?>

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    (questionId) => globalThis.__fmOpenDb().then((db) => new Promise((resolve, reject) => {
        const tx = db.transaction(['cards'], 'readwrite');
        const store = tx.objectStore('cards');
        const getReq = store.get(questionId);
        getReq.onsuccess = () => {
            if (getReq.result) {
                getReq.result.isHidden = true;
                store.put(getReq.result);
            }
        };
        tx.oncomplete = () => { db.close(); resolve(null); };
        tx.onerror = () => { db.close(); reject(tx.error); };
    }))
    """,
)
internal external fun idbMarkHidden(questionId: Int): Promise<JsAny?>

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    () => globalThis.__fmOpenDb().then((db) => new Promise((resolve, reject) => {
        const tx = db.transaction(['cards'], 'readwrite');
        const cursorReq = tx.objectStore('cards').openCursor();
        cursorReq.onsuccess = (event) => {
            const cursor = event.target.result;
            if (cursor) {
                if (cursor.value.isHidden) {
                    const updated = cursor.value;
                    updated.isHidden = false;
                    cursor.update(updated);
                }
                cursor.continue();
            }
        };
        tx.oncomplete = () => { db.close(); resolve(null); };
        tx.onerror = () => { db.close(); reject(tx.error); };
    }))
    """,
)
internal external fun idbResetAllHidden(): Promise<JsAny?>
