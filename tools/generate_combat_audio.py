"""Create the 26 original one-shot combat cues used by the mod.

Requires NumPy and FFmpeg with a Vorbis encoder. The WAV intermediates live in
the system temporary directory; only game-ready OGG files are written out.
"""

from __future__ import annotations

import argparse
import shutil
import subprocess
import tempfile
import wave
from pathlib import Path

import numpy as np


SAMPLE_RATE = 24000
EVENTS = [
    ("hunt_strike", "hunt", False), ("hunt_reverse", "hunt", True),
    ("toughness_hit", "toughness", False), ("toughness_break", "toughness", True),
    ("preservation_wall", "preservation", False), ("preservation_counter", "preservation", True),
    ("destruction_rage", "destruction", False), ("destruction_burn", "destruction", False),
    ("destruction_desperate", "destruction", True),
    ("erudition_echo", "erudition", False), ("erudition_final", "erudition", True),
    ("nihility_mark", "nihility", False), ("nihility_spread", "nihility", False),
    ("nihility_end", "nihility", True),
    ("harmony_resonance", "harmony", False), ("harmony_afterglow", "harmony", False),
    ("harmony_concert", "harmony", False), ("harmony_unison", "harmony", True),
    ("abundance_heal", "abundance", False), ("abundance_manna", "abundance", False),
    ("abundance_mercy", "abundance", True),
    ("remembrance_record", "remembrance", False), ("remembrance_echo", "remembrance", False),
    ("remembrance_eternal", "remembrance", True),
    ("elation_burst", "elation", False), ("elation_grand", "elation", True),
]


def envelope(t: np.ndarray, decay: float, attack: float = 0.006) -> np.ndarray:
    return np.minimum(1.0, t / attack) * np.exp(-t / decay)


def add_tone(out: np.ndarray, t: np.ndarray, frequency: float, start: float,
             amplitude: float, decay: float, partials=(1.0, 0.28, 0.10)) -> None:
    local = t - start
    mask = local >= 0
    x = local[mask]
    env = envelope(x, decay)
    tone = np.zeros_like(x)
    for index, strength in enumerate(partials, 1):
        tone += strength * np.sin(2 * np.pi * frequency * index * x + index * index * 0.11)
    out[mask] += amplitude * env * tone


def add_sweep(out: np.ndarray, t: np.ndarray, f0: float, f1: float,
              start: float, length: float, amplitude: float, decay: float) -> None:
    local = t - start
    mask = (local >= 0) & (local <= length)
    x = local[mask]
    phase = 2 * np.pi * (f0 * x + (f1 - f0) * x * x / (2 * length))
    env = envelope(x, decay) * np.minimum(1.0, np.maximum(0.0, (length - x) / 0.018))
    out[mask] += amplitude * env * np.sin(phase)


def add_noise(out: np.ndarray, t: np.ndarray, rng: np.random.Generator,
              start: float, length: float, amplitude: float,
              cutoff: float, lowpass: bool, decay: float) -> None:
    noise = rng.normal(0, 1, len(t))
    spectrum = np.fft.rfft(noise)
    bins = np.fft.rfftfreq(len(t), 1 / SAMPLE_RATE)
    if lowpass:
        gain = 1 / np.sqrt(1 + (bins / cutoff) ** 6)
    else:
        gain = bins / np.sqrt(bins * bins + cutoff * cutoff)
    shaped = np.fft.irfft(spectrum * gain, n=len(t))
    local = t - start
    mask = (local >= 0) & (local <= length)
    x = local[mask]
    env = envelope(x, decay) * np.minimum(1.0, np.maximum(0.0, (length - x) / 0.016))
    out[mask] += amplitude * env * shaped[mask]


def synthesize(family: str, strong: bool, variant: int, seed: int) -> np.ndarray:
    duration = 0.68 if strong else 0.48
    count = int(SAMPLE_RATE * duration)
    t = np.arange(count, dtype=np.float64) / SAMPLE_RATE
    out = np.zeros(count, dtype=np.float64)
    rng = np.random.default_rng(seed)
    shift = 1 + ((variant % 5) - 2) * 0.018
    power = 1.20 if strong else 1.0

    if family == "hunt":
        add_noise(out, t, rng, 0, .24, .20, 1500, False, .085)
        add_sweep(out, t, 1650 * shift, 330 * shift, .005, .27, .32, .12)
        add_tone(out, t, 740 * shift, .12, .10, .18, (1, .16, .05))
        if strong:
            add_sweep(out, t, 1300 * shift, 470 * shift, .20, .30, .22, .20)
    elif family == "toughness":
        add_noise(out, t, rng, 0, .12 if not strong else .25, .28 if strong else .13,
                  1800, True, .06 if not strong else .16)
        add_tone(out, t, (530 if strong else 780) * shift, .003, .25 if strong else .15,
                 .18 if strong else .10, (1, .44, .17, .06))
        if strong:
            add_sweep(out, t, 210, 68, .02, .28, .23, .19)
            add_tone(out, t, 1170 * shift, .08, .09, .30, (1, .24, .07))
    elif family == "preservation":
        add_noise(out, t, rng, 0, .16, .14, 1100, True, .07)
        add_sweep(out, t, 145 * shift, 72 * shift, .002, .19, .28, .13)
        add_tone(out, t, 440 * shift, .012, .22, .31, (1, .62, .34, .15))
        add_tone(out, t, 790 * shift, .025, .09, .24, (1, .24, .07))
        if strong:
            add_tone(out, t, 1180 * shift, .07, .12, .36, (1, .26, .06))
            add_sweep(out, t, 270, 110, .12, .28, .18, .25)
    elif family == "destruction":
        add_noise(out, t, rng, 0, .34 if strong else .24, .32 if strong else .23,
                  1050, True, .16 if strong else .105)
        add_noise(out, t, rng, 0, .16, .08, 2400, False, .055)
        add_sweep(out, t, 128 * shift, 43 * shift, .002, .32 if strong else .22,
                  .45 if strong else .31, .24 if strong else .15)
        add_sweep(out, t, 540 * shift, 115 * shift, .015, .23, .16, .12)
        if strong:
            add_tone(out, t, 690 * shift, .10, .10, .24, (1, .35, .08))
    elif family == "erudition":
        base = [470, 640, 930, 1250][variant % 4] * shift
        for index, freq in enumerate((base, base * 1.25, base * 1.62)):
            add_tone(out, t, freq, .018 + index * .073, .18 if not strong else .22,
                     .20 if not strong else .30, (1, .35, .11, .035))
        add_noise(out, t, rng, .01, .27, .055, 2600, False, .12)
        if strong:
            add_tone(out, t, base * 2.04, .26, .14, .34, (1, .3, .1))
            add_sweep(out, t, 310, 900, .02, .38, .10, .30)
    elif family == "nihility":
        add_noise(out, t, rng, .01, .38 if strong else .29, .23, 620, True,
                  .27 if strong else .19)
        add_sweep(out, t, 330 * shift, 74 * shift, .01, .35 if strong else .27,
                  .26 if strong else .19, .22 if strong else .16)
        add_tone(out, t, 156 * shift, .02, .12, .26, (1, .28, .07))
        if strong:
            add_noise(out, t, rng, .25, .34, .16, 900, False, .24)
            add_tone(out, t, 92 * shift, .22, .17, .34, (1, .32, .09))
    elif family == "harmony":
        base = [392, 440, 523, 587][variant % 4] * shift
        for index, ratio in enumerate((1, 1.25, 1.5, 2)):
            add_tone(out, t, base * ratio, .012 + index * .048, .14 if not strong else .17,
                     .27 if not strong else .39, (1, .25, .09, .025))
        add_noise(out, t, rng, 0, .30, .045, 3200, False, .16)
        if strong:
            add_tone(out, t, base * 2.5, .19, .11, .36, (1, .28, .08))
    elif family == "abundance":
        base = [330, 370, 415][variant % 3] * shift
        add_sweep(out, t, base, base * 1.42, .01, .30, .19, .22)
        add_tone(out, t, base * 1.5, .08, .16, .33, (1, .32, .10))
        add_tone(out, t, base * 2.0, .16, .09, .36, (1, .25, .07))
        add_noise(out, t, rng, .02, .34, .045, 2700, False, .22)
        if strong:
            add_tone(out, t, base * 2.4, .22, .12, .38, (1, .23, .06))
    elif family == "remembrance":
        base = [740, 830, 932][variant % 3] * shift
        for index, ratio in enumerate((1, 1.5, 2.02, 2.51)):
            add_tone(out, t, base * ratio, .008 + index * .055, .14 if not strong else .17,
                     .31 if not strong else .43, (1, .42, .18, .06))
        add_noise(out, t, rng, 0, .33, .055, 2000, False, .25)
        if strong:
            add_sweep(out, t, 1500, 640, .18, .34, .16, .31)
    elif family == "elation":
        base = [520, 590, 660, 740][variant % 4] * shift
        for index, ratio in enumerate((1, 1.33, 1.78)):
            add_tone(out, t, base * ratio, .01 + index * .095, .18 if not strong else .20,
                     .16 if not strong else .21, (1, .16, .035))
        add_noise(out, t, rng, .03, .31, .07, 3900, False, .19)
        if strong:
            add_tone(out, t, base * 2.35, .33, .16, .28, (1, .24, .06))
            add_sweep(out, t, 950, 1500, .30, .19, .12, .18)

    # Quiet detail layers add a low body, bright attack, or resonant overtones
    # without changing each path's main motif or making the cues much longer.
    if family == "hunt":
        add_noise(out, t, rng, .002, .085, .075, 2600, False, .045)
        add_sweep(out, t, 2380 * shift, 820 * shift, .004, .12, .085, .065)
        add_tone(out, t, 920 * shift, .18, .045, .12, (1, .30, .08))
    elif family == "toughness":
        add_noise(out, t, rng, .002, .055, .14, 2850, False, .035)
        add_sweep(out, t, 118 * shift, 46 * shift, .006, .16, .12, .115)
    elif family == "preservation":
        add_tone(out, t, 1360 * shift, .045, .075, .31, (1, .20, .055, .018))
        add_tone(out, t, 2240 * shift, .085, .038, .24, (1, .12, .025))
    elif family == "destruction":
        add_sweep(out, t, 82 * shift, 34 * shift, .008, .23, .13, .19)
        add_noise(out, t, rng, .018, .12, .065, 2300, False, .075)
    elif family == "erudition":
        shimmer = [1640, 1810, 2050, 2290][variant % 4] * shift
        add_tone(out, t, shimmer, .055, .065, .29, (1, .34, .11, .035))
        add_tone(out, t, shimmer * 1.51, .17, .032, .25, (1, .22, .06))
    elif family == "nihility":
        add_noise(out, t, rng, .075, .25, .075, 1450, True, .22)
        add_sweep(out, t, 205 * shift, 58 * shift, .12, .24, .10, .20)
    elif family == "harmony":
        base = [392, 440, 523, 587][variant % 4] * shift
        add_tone(out, t, base * 2.51, .10, .07, .36, (1, .28, .08, .025))
        add_tone(out, t, base * 3.02, .20, .035, .30, (1, .2, .05))
    elif family == "abundance":
        bell = [988, 1108, 1245][variant % 3] * shift
        add_tone(out, t, bell, .11, .075, .38, (1, .30, .095, .03))
        add_tone(out, t, bell * 1.5, .23, .035, .30, (1, .22, .055))
    elif family == "remembrance":
        glass = [1760, 1980, 2210][variant % 3] * shift
        add_tone(out, t, glass, .035, .07, .40, (1, .26, .08, .025))
        add_tone(out, t, glass * 1.414, .14, .032, .34, (1, .2, .05))
    elif family == "elation":
        sparkle = [1320, 1480, 1660, 1860][variant % 4] * shift
        add_tone(out, t, sparkle, .035, .065, .19, (1, .20, .045))
        add_tone(out, t, sparkle * 1.27, .16, .045, .22, (1, .17, .035))

    # Tiny delayed reflections add body while keeping the cue short and readable.
    echo = np.zeros_like(out)
    echo[900:] += out[:-900] * .20
    echo[1800:] += out[:-1800] * .09
    out += echo
    if family in {"preservation", "erudition", "harmony", "abundance", "remembrance", "elation"}:
        room = np.zeros_like(out)
        room[3100:] += out[:-3100] * .085
        room[5200:] += out[:-5200] * .035
        out += room
    out *= np.minimum(1.0, np.maximum(0.0, (duration - t) / .025))
    peak = float(np.max(np.abs(out))) or 1.0
    return (out * (.82 / peak)).astype(np.float32)


def write_wav(path: Path, samples: np.ndarray) -> None:
    pcm = np.clip(samples * 32767, -32768, 32767).astype("<i2")
    with wave.open(str(path), "wb") as output:
        output.setnchannels(1)
        output.setsampwidth(2)
        output.setframerate(SAMPLE_RATE)
        output.writeframes(pcm.tobytes())


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--ffmpeg", default="ffmpeg")
    parser.add_argument("--encoder", default="libvorbis")
    parser.add_argument("--output-dir", required=True, type=Path)
    args = parser.parse_args()
    ffmpeg = shutil.which(args.ffmpeg) or args.ffmpeg
    args.output_dir.mkdir(parents=True, exist_ok=True)

    with tempfile.TemporaryDirectory(prefix="starrail_combat_audio_") as temp:
        temp_dir = Path(temp)
        for index, (name, family, strong) in enumerate(EVENTS):
            wav_path = temp_dir / f"{name}.wav"
            ogg_path = args.output_dir / f"{name}.ogg"
            write_wav(wav_path, synthesize(family, strong, index, 122000 + index * 37))
            subprocess.run([
                ffmpeg, "-hide_banner", "-loglevel", "error", "-y", "-i", str(wav_path),
                "-ar", str(SAMPLE_RATE), "-ac", "1", "-strict", "-2",
                "-c:a", args.encoder, "-qscale:a", "5",
                str(ogg_path),
            ], check=True)
            print(f"created {ogg_path.name}")


if __name__ == "__main__":
    main()
