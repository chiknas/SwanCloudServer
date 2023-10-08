// Functions to keep a mobile browser awake when needed
const canWakeLock = () => "wakeLock" in navigator;

let wakelock;
// Try to request a lock to keep the screen open on mobiles
async function lockWakeState() {
  if (!canWakeLock()) return;
  try {
    wakelock = await navigator.wakeLock.request();
    wakelock.addEventListener("release", () => {
      console.log("Screen Wake State Locked:", !wakelock.released);
    });
    console.log("Screen Wake State Locked:", !wakelock.released);
  } catch (e) {
    console.error("Failed to lock wake state with reason:", e.message);
  }
}

// Release the lock to let the screen go to sleep if needed
function releaseWakeState() {
  if (wakelock) wakelock.release();
  wakelock = null;
}
