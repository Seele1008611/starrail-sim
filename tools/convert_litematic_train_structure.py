"""Customize the full reference Litematica train and export its 1.20.1 structure NBT.

The original schematic remains read-only. This script writes the customized Litematica
archive and the runtime StructureTemplate using only the Python standard library.
"""

from __future__ import annotations

import argparse
import copy
import gzip
import json
import math
import struct
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
REFERENCE_SOURCE = ROOT / "设计方案" / "模型资产" / "星穹列车.litematic"
CUSTOMIZED_SOURCE = ROOT / "设计方案" / "模型资产" / "星穹列车_1.23.0枢纽定制.litematic"
SOURCE = CUSTOMIZED_SOURCE
OUTPUT = ROOT / "src" / "main" / "resources" / "data" / "starrail_sim" / "structures" / "astral_express.nbt"
DATA_VERSION = 3465  # Minecraft 1.20.1
TRIM_MIN = (1, 0, 10)
REFERENCE_SIZE = (258, 23, 23)
# Preserve the complete original train relative to itself. Add two room coaches
# beyond the original lounge-car tail and reuse the original 10-block transition
# between the added coaches and again at the interface with the original train.
COACH_LENGTH = 73
ROOM_SOURCE_START = 90
ROOM_SOURCE_END = 163
TAIL_CONNECTOR_SOURCE_START = 80
TAIL_CONNECTOR_SOURCE_END = 90
ROOM_ONE_START = 0
MID_CONNECTOR_START = ROOM_ONE_START + COACH_LENGTH
MID_CONNECTOR_END = MID_CONNECTOR_START + (TAIL_CONNECTOR_SOURCE_END - TAIL_CONNECTOR_SOURCE_START)
ROOM_TWO_START = MID_CONNECTOR_END
ROOM_TWO_END = ROOM_TWO_START + COACH_LENGTH
TAIL_CONNECTOR_START = ROOM_TWO_END
ORIGINAL_TRAIN_START = TAIL_CONNECTOR_START + (TAIL_CONNECTOR_SOURCE_END - TAIL_CONNECTOR_SOURCE_START)
ORIGINAL_SHIFT = ORIGINAL_TRAIN_START
EXPECTED_SIZE = (REFERENCE_SIZE[0] + ORIGINAL_SHIFT, 23, 23)
BEACON_LOCAL_POS = (131 + ORIGINAL_SHIFT, 6, 11)
NEW_COACH_RANGES = (
    (ROOM_ONE_START, ROOM_ONE_START + COACH_LENGTH),
    (ROOM_TWO_START, ROOM_TWO_END),
)
HEAD_SHIFT = ORIGINAL_SHIFT
CONNECTOR_LANTERN_POSITIONS = (
    (MID_CONNECTOR_START + 5, 10, 9), (MID_CONNECTOR_START + 5, 10, 13),
    (TAIL_CONNECTOR_START + 4, 10, 9), (TAIL_CONNECTOR_START + 4, 10, 13),
)
CONNECTOR_SHROOMLIGHT_POSITIONS = (
    (MID_CONNECTOR_START + 6, 11, 8), (MID_CONNECTOR_START + 6, 11, 14),
    (TAIL_CONNECTOR_START + 3, 11, 8), (TAIL_CONNECTOR_START + 3, 11, 14),
)
CONNECTOR_LANTERN_STATE = ("minecraft:lantern", (("hanging", "true"), ("waterlogged", "false")))
CONNECTOR_SHROOMLIGHT_STATE = ("minecraft:shroomlight", ())
HEAD_CAB_LANTERN_POSITIONS = tuple(
    (x + HEAD_SHIFT, 9, z) for x in (231, 235, 239) for z in (6, 16)
)
HEAD_CAB_LANTERN_STATE = ("minecraft:lantern", (("hanging", "false"),))
# The reference head has large dark rooms whose decorative shroomlights sit
# below solid flooring. These ceiling lanterns light those interior spaces.
HEAD_INTERIOR_LANTERN_POSITIONS = (
    *((x + HEAD_SHIFT, 16, z) for x in (169, 173, 177, 181, 185, 189) for z in (8, 11, 14)),
    *((x + HEAD_SHIFT, 16, 11) for x in (211, 215, 219, 223, 227, 231, 235, 239, 243)),
    (247 + HEAD_SHIFT, 14, 11),
)
HEAD_INTERIOR_LANTERN_STATE = ("minecraft:lantern", (("hanging", "true"),))
HEAD_FLOOR_LANTERN_POSITIONS = ((247 + HEAD_SHIFT, 5, 9), (247 + HEAD_SHIFT, 5, 13))
HEAD_FLOOR_LANTERN_STATE = ("minecraft:lantern", (("hanging", "false"),))
HEAD_TRANSITION_LANTERN_POSITIONS = ((167 + HEAD_SHIFT, 5, 11),)
HEAD_TRANSITION_LANTERN_STATE = ("minecraft:lantern", (("hanging", "false"),))
HEAD_LOWER_PASSAGE_LIGHT_POSITIONS = tuple((x + HEAD_SHIFT, 5, 11) for x in range(211, 244, 4))
HEAD_LOWER_PASSAGE_LIGHT_STATE = ("minecraft:shroomlight", ())
HEAD_INTERIOR_FLOOR_LIGHT_POSITIONS = ((196 + HEAD_SHIFT, 15, 11), (245 + HEAD_SHIFT, 15, 11))
HEAD_CRAWLSPACE_FLOOR_LIGHT_POSITIONS = ((206 + HEAD_SHIFT, 4, 11),)
HEAD_FLOOR_SHROOMLIGHT_STATE = ("minecraft:shroomlight", ())
# These recessed floor fixtures cover head side passages, raised pockets, and
# the small dark cells at the ends of the reference vestibules.
TRAIN_DARK_POCKET_SHROOMLIGHT_POSITIONS = (
    (397, 6, 3), (397, 6, 19),
    (365, 5, 7), (365, 5, 15),
    (383, 13, 17), (383, 13, 5),
    (417, 2, 21), (417, 2, 1),
    (423, 1, 3), (423, 1, 19),
    (396, 13, 5), (396, 13, 17),
    (378, 13, 5), (378, 13, 17),
    (354, 12, 4), (354, 12, 18),
    (361, 14, 14), (361, 14, 8),
    (165, 7, 15), (165, 7, 7),
    (386, 13, 5), (386, 13, 17),
    (246, 4, 14), (246, 4, 8),
    (73, 4, 14), (73, 4, 8),
)
# The reference passenger coaches also contain enclosed dark pockets. These
# fixtures light the first coach's side niches and upper vestibule, the three
# repeated underfloor cavities, and the short coach-end ramps.
COACH_A_INTERIOR_LANTERN_SOURCE_POSITIONS = (
    (61, 12, 11), (65, 12, 11), (77, 17, 13), (77, 17, 9),
)
COACH_A_INTERIOR_LANTERN_POSITIONS = tuple(
    (x + ORIGINAL_SHIFT, y, z) for x, y, z in COACH_A_INTERIOR_LANTERN_SOURCE_POSITIONS
)
COACH_A_INTERIOR_LANTERN_STATE = ("minecraft:lantern", (("hanging", "true"),))
COACH_A_SHROOMLIGHT_SOURCE_POSITIONS = (
    (52, 8, 18), (52, 8, 4),
    (56, 5, 10), (56, 5, 12), (56, 5, 16), (56, 5, 6),
    (64, 5, 18), (64, 5, 4),
    (69, 5, 17), (69, 5, 5),
)
COACH_A_SHROOMLIGHT_POSITIONS = tuple(
    (x + ORIGINAL_SHIFT, y, z) for x, y, z in COACH_A_SHROOMLIGHT_SOURCE_POSITIONS
)
COACH_A_SHROOMLIGHT_DEEPSLATE_POSITIONS = {
    (52 + ORIGINAL_SHIFT, 8, 18), (52 + ORIGINAL_SHIFT, 8, 4),
    (64 + ORIGINAL_SHIFT, 5, 18), (64 + ORIGINAL_SHIFT, 5, 4),
}
ROOM_SOURCE_UNDERFLOOR_LIGHT_POSITIONS = tuple((x, 3, 11) for x in (140, 144, 148))
ROOM_SOURCE_RAMP_LANTERN_POSITIONS = ((150, 9, 10), (150, 9, 12))
ROOM_SOURCE_END_LIGHT_POSITIONS = ((160, 4, 16),)
ROOM_SOURCE_END_MIRRORED_LIGHT_POSITIONS = tuple(
    (x, y, 22 - z) for x, y, z in ROOM_SOURCE_END_LIGHT_POSITIONS
)


def room_target_x(source_x: int, target_start: int, reversed_x: bool = False) -> int:
    offset = ROOM_SOURCE_END - 1 - source_x if reversed_x else source_x - ROOM_SOURCE_START
    return target_start + offset


ROOM_LIGHT_POSITIONS = tuple(
    (room_target_x(x, ROOM_ONE_START), y, z) for x, y, z in ROOM_SOURCE_UNDERFLOOR_LIGHT_POSITIONS
) + tuple(
    (room_target_x(x, ROOM_TWO_START, True), y, z) for x, y, z in ROOM_SOURCE_UNDERFLOOR_LIGHT_POSITIONS
)
ORIGINAL_ROOM_UNDERFLOOR_LIGHT_POSITIONS = tuple(
    (x + ORIGINAL_SHIFT, y, z) for x, y, z in ROOM_SOURCE_UNDERFLOOR_LIGHT_POSITIONS
)
COACH_UNDERFLOOR_LIGHT_POSITIONS = (*ROOM_LIGHT_POSITIONS, *ORIGINAL_ROOM_UNDERFLOOR_LIGHT_POSITIONS)
COACH_END_LIGHT_POSITIONS = tuple(
    (room_target_x(x, ROOM_ONE_START), y, z) for x, y, z in ROOM_SOURCE_END_LIGHT_POSITIONS
) + tuple(
    (room_target_x(x, ROOM_TWO_START, True), y, z) for x, y, z in ROOM_SOURCE_END_LIGHT_POSITIONS
) + tuple(
    (x + ORIGINAL_SHIFT, y, z) for x, y, z in ROOM_SOURCE_END_LIGHT_POSITIONS
)
COACH_END_MIRRORED_LIGHT_POSITIONS = tuple(
    (room_target_x(x, ROOM_ONE_START), y, z) for x, y, z in ROOM_SOURCE_END_MIRRORED_LIGHT_POSITIONS
) + tuple(
    (room_target_x(x, ROOM_TWO_START, True), y, z) for x, y, z in ROOM_SOURCE_END_MIRRORED_LIGHT_POSITIONS
) + tuple(
    (x + ORIGINAL_SHIFT, y, z) for x, y, z in ROOM_SOURCE_END_MIRRORED_LIGHT_POSITIONS
)
COACH_SHROOMLIGHT_POSITIONS = (
    *COACH_A_SHROOMLIGHT_POSITIONS,
    *COACH_UNDERFLOOR_LIGHT_POSITIONS,
    *COACH_END_LIGHT_POSITIONS,
    *COACH_END_MIRRORED_LIGHT_POSITIONS,
)
COACH_SHROOMLIGHT_STATE = ("minecraft:shroomlight", ())
COACH_RAMP_LANTERN_POSITIONS = tuple(
    (room_target_x(x, ROOM_ONE_START), y, z) for x, y, z in ROOM_SOURCE_RAMP_LANTERN_POSITIONS
) + tuple(
    (room_target_x(x, ROOM_TWO_START, True), y, z) for x, y, z in ROOM_SOURCE_RAMP_LANTERN_POSITIONS
) + tuple(
    (x + ORIGINAL_SHIFT, y, z) for x, y, z in ROOM_SOURCE_RAMP_LANTERN_POSITIONS
)
COACH_RAMP_LANTERN_STATE = ("minecraft:lantern", (("hanging", "false"),))
ROOM_DOORWAY_AIR_POSITIONS = (
    *((ROOM_ONE_START + COACH_LENGTH - 1, y, z) for z in (10, 12) for y in (6, 7)),
    *((ROOM_TWO_START, y, z) for z in (10, 12) for y in (6, 7)),
    *((ROOM_TWO_END - 1, y, z) for z in (10, 12) for y in (6, 7)),
    *((ORIGINAL_TRAIN_START, y, z) for z in (10, 12) for y in (6, 7)),
    *((ORIGINAL_TRAIN_START + ROOM_SOURCE_END - 1, y, 11) for y in (6, 7)),
)
TAIL_CAP_INTERIOR_POSITIONS = tuple(
    (ROOM_ONE_START, y, z) for y in range(5, 18) for z in range(6, 17)
)


def validate_added_light_symmetry() -> None:
    light_groups = {
        "head cabin": HEAD_CAB_LANTERN_POSITIONS,
        "head interior": HEAD_INTERIOR_LANTERN_POSITIONS,
        "head floor": HEAD_FLOOR_LANTERN_POSITIONS,
        "head transition": HEAD_TRANSITION_LANTERN_POSITIONS,
        "head lower passage": HEAD_LOWER_PASSAGE_LIGHT_POSITIONS,
        "head floor inserts": (*HEAD_INTERIOR_FLOOR_LIGHT_POSITIONS, *HEAD_CRAWLSPACE_FLOOR_LIGHT_POSITIONS),
        "head and connector dark pockets": TRAIN_DARK_POCKET_SHROOMLIGHT_POSITIONS,
        "vestibule lanterns": CONNECTOR_LANTERN_POSITIONS,
        "vestibule floor lights": CONNECTOR_SHROOMLIGHT_POSITIONS,
        "lounge upper vestibule": COACH_A_INTERIOR_LANTERN_POSITIONS,
        "lounge recessed lights": COACH_A_SHROOMLIGHT_POSITIONS,
        "coach underfloor": COACH_UNDERFLOOR_LIGHT_POSITIONS,
        "coach end inserts": (*COACH_END_LIGHT_POSITIONS, *COACH_END_MIRRORED_LIGHT_POSITIONS),
        "coach ramp lanterns": COACH_RAMP_LANTERN_POSITIONS,
    }
    for label, positions in light_groups.items():
        position_set = set(positions)
        unmatched = [
            pos for pos in position_set if pos[2] != 11
            and (pos[0], pos[1], 22 - pos[2]) not in position_set
        ]
        if unmatched:
            raise ValueError(f"New {label} light positions are not symmetric across the car: {unmatched}")


@dataclass
class Tag:
    type_id: int
    value: Any


class NbtReader:
    def __init__(self, data: bytes):
        self.data = data
        self.offset = 0

    def read(self, count: int) -> bytes:
        result = self.data[self.offset:self.offset + count]
        if len(result) != count:
            raise ValueError("Unexpected end of NBT data")
        self.offset += count
        return result

    def number(self, fmt: str) -> Any:
        return struct.unpack(">" + fmt, self.read(struct.calcsize(">" + fmt)))[0]

    def string(self) -> str:
        return self.read(self.number("H")).decode("utf-8")

    def payload(self, type_id: int) -> Tag:
        if type_id == 1: return Tag(type_id, self.number("b"))
        if type_id == 2: return Tag(type_id, self.number("h"))
        if type_id == 3: return Tag(type_id, self.number("i"))
        if type_id == 4: return Tag(type_id, self.number("q"))
        if type_id == 5: return Tag(type_id, self.number("f"))
        if type_id == 6: return Tag(type_id, self.number("d"))
        if type_id == 7:
            length = self.number("i")
            return Tag(type_id, self.read(length))
        if type_id == 8: return Tag(type_id, self.string())
        if type_id == 9:
            element_type = self.number("B")
            length = self.number("i")
            return Tag(type_id, (element_type, [self.payload(element_type) for _ in range(length)]))
        if type_id == 10:
            compound = {}
            while True:
                child_type = self.number("B")
                if child_type == 0:
                    break
                name = self.string()
                compound[name] = self.payload(child_type)
            return Tag(type_id, compound)
        if type_id == 11:
            return Tag(type_id, [self.number("i") for _ in range(self.number("i"))])
        if type_id == 12:
            return Tag(type_id, [self.number("q") for _ in range(self.number("i"))])
        raise ValueError(f"Unsupported NBT tag id {type_id}")

    def root(self) -> tuple[str, Tag]:
        type_id = self.number("B")
        if type_id != 10:
            raise ValueError("Litematica root is not a compound")
        return self.string(), self.payload(type_id)


def nbt_string(value: str) -> Tag: return Tag(8, value)
def nbt_int(value: int) -> Tag: return Tag(3, value)
def nbt_double(value: float) -> Tag: return Tag(6, float(value))
def nbt_compound(value: dict[str, Tag]) -> Tag: return Tag(10, value)
def nbt_int_list(value: list[int]) -> Tag: return Tag(9, (3, [nbt_int(x) for x in value]))
def nbt_double_list(value: list[float]) -> Tag: return Tag(9, (6, [nbt_double(x) for x in value]))
def nbt_compound_list(value: list[Tag]) -> Tag: return Tag(9, (10, value))


def write_string(value: str) -> bytes:
    raw = value.encode("utf-8")
    if len(raw) > 65535:
        raise ValueError("NBT string exceeds 65535 bytes")
    return struct.pack(">H", len(raw)) + raw


def write_payload(tag: Tag) -> bytes:
    type_id, value = tag.type_id, tag.value
    if type_id == 1: return struct.pack(">b", value)
    if type_id == 2: return struct.pack(">h", value)
    if type_id == 3: return struct.pack(">i", value)
    if type_id == 4: return struct.pack(">q", value)
    if type_id == 5: return struct.pack(">f", value)
    if type_id == 6: return struct.pack(">d", value)
    if type_id == 7: return struct.pack(">i", len(value)) + value
    if type_id == 8: return write_string(value)
    if type_id == 9:
        element_type, values = value
        return struct.pack(">Bi", element_type, len(values)) + b"".join(write_payload(x) for x in values)
    if type_id == 10:
        output = bytearray()
        for name, child in value.items():
            output.extend(struct.pack(">B", child.type_id))
            output.extend(write_string(name))
            output.extend(write_payload(child))
        output.append(0)
        return bytes(output)
    if type_id == 11:
        return struct.pack(">i", len(value)) + b"".join(struct.pack(">i", x) for x in value)
    if type_id == 12:
        return struct.pack(">i", len(value)) + b"".join(struct.pack(">q", x) for x in value)
    raise ValueError(f"Unsupported NBT tag id {type_id}")


def write_root(name: str, tag: Tag) -> bytes:
    return struct.pack(">B", tag.type_id) + write_string(name) + write_payload(tag)


def field(tag: Tag, name: str) -> Tag:
    if tag.type_id != 10:
        raise ValueError(f"Expected compound while reading {name}")
    return tag.value[name]


def scalar(tag: Tag) -> Any:
    return tag.value


def int_vector(tag: Tag) -> tuple[int, int, int]:
    if tag.type_id == 10:
        values = [scalar(tag.value[key]) for key in ("x", "y", "z")]
    elif tag.type_id == 11:
        values = tag.value
    elif tag.type_id == 9:
        values = [scalar(v) for v in tag.value[1]]
    else:
        raise ValueError("Expected integer vector")
    if len(values) != 3:
        raise ValueError("Expected a three-component vector")
    return tuple(int(v) for v in values)


def string_value(tag: Tag) -> str:
    if tag.type_id != 8:
        raise ValueError("Expected NBT string")
    return tag.value


def nbt_compound_to_python(tag: Tag) -> dict[str, Any]:
    if tag.type_id != 10:
        raise ValueError("Expected a compound")
    return {key: nbt_compound_to_python(value) if value.type_id == 10 else
            [nbt_compound_to_python(v) if v.type_id == 10 else v.value
             for v in value.value[1]] if value.type_id == 9 else value.value
            for key, value in tag.value.items()}


def read_litematic(source_path: Path | None = None, expected_size: tuple[int, int, int] | None = None) -> tuple[Tag, dict[tuple[int, int, int], Tag], dict[tuple[int, int, int], Tag], list[tuple[Tag, list[float]]]]:
    source_path = source_path or SOURCE
    if expected_size is None:
        expected_size = REFERENCE_SIZE if source_path.resolve() == REFERENCE_SOURCE.resolve() else EXPECTED_SIZE
    _, root = NbtReader(gzip.decompress(source_path.read_bytes())).root()
    metadata = field(root, "Metadata")
    regions = field(root, "Regions").value
    if len(regions) != 1:
        raise ValueError(f"Expected one Litematica region; found {len(regions)}")

    block_states: dict[tuple[int, int, int], Tag] = {}
    block_entities: dict[tuple[int, int, int], Tag] = {}
    entity_data: list[tuple[Tag, tuple[int, int, int]]] = []

    for region_name, region in regions.items():
        signed_size = int_vector(field(region, "Size"))
        region_position = int_vector(field(region, "Position"))
        size = tuple(abs(x) for x in signed_size)
        origin = tuple(region_position[i] + min(0, signed_size[i] + 1) for i in range(3))
        palette = field(region, "BlockStatePalette").value[1]
        palette_size = len(palette)
        bits = max(2, (palette_size - 1).bit_length())
        mask = (1 << bits) - 1
        packed = [x & ((1 << 64) - 1) for x in field(region, "BlockStates").value]

        volume = math.prod(size)
        for index in range(volume):
            bit_index = index * bits
            word_index, bit_offset = divmod(bit_index, 64)
            value = packed[word_index] >> bit_offset
            if bit_offset + bits > 64:
                value |= packed[word_index + 1] << (64 - bit_offset)
            state = palette[value & mask]
            x = index % size[0]
            z = index // size[0] % size[2]
            y = index // (size[0] * size[2])
            block_states[(origin[0] + x, origin[1] + y, origin[2] + z)] = state

        for block_entity in field(region, "TileEntities").value[1]:
            coords = block_entity.value
            pos = (int(scalar(coords["x"])), int(scalar(coords["y"])), int(scalar(coords["z"])))
            if pos in block_entities:
                raise ValueError(f"Duplicate block entity position in region {region_name}: {pos}")
            block_entities[pos] = block_entity

        # Litematica stores entity positions relative to the region's original anchor;
        # block-state indices above are already normalized to the region's minimum corner.
        for entity in field(region, "Entities").value[1]:
            nbt = entity.value
            raw_pos = [scalar(v) for v in nbt["Pos"].value[1]]
            normalized = tuple(region_position[i] + math.floor(raw_pos[i]) - origin[i] for i in range(3))
            entity_data.append((entity, normalized))

    if not block_states:
        raise ValueError("Litematica region contains no block states")
    occupied = [pos for pos, state in block_states.items()
                if string_value(field(state, "Name")) not in ("minecraft:air", "minecraft:cave_air", "minecraft:void_air")]
    actual_min = tuple(min(pos[i] for pos in occupied) for i in range(3))
    actual_max = tuple(max(pos[i] for pos in occupied) for i in range(3))
    if actual_min != TRIM_MIN:
        raise ValueError(f"Source occupied minimum changed: expected {TRIM_MIN}, found {actual_min}")
    size = tuple(actual_max[i] - actual_min[i] + 1 for i in range(3))
    if size != expected_size:
        raise ValueError(f"Source occupied size changed: expected {expected_size}, found {size}")

    trimmed = {(pos[0] - actual_min[0], pos[1] - actual_min[1], pos[2] - actual_min[2]): state
               for pos, state in block_states.items()
               if all(actual_min[i] <= pos[i] <= actual_max[i] for i in range(3))}
    trimmed_be = {(pos[0] - actual_min[0], pos[1] - actual_min[1], pos[2] - actual_min[2]): value
                  for pos, value in block_entities.items()
                  if all(actual_min[i] <= pos[i] <= actual_max[i] for i in range(3))}
    normalized_entities = []
    for entity, _ in entity_data:
        pos_tag = entity.value["Pos"]
        raw_pos = [float(scalar(v)) for v in pos_tag.value[1]]
        normalized_pos = [region_position[i] + raw_pos[i] - actual_min[i] for i in range(3)]
        normalized_entities.append((entity, normalized_pos))
    return metadata, trimmed, trimmed_be, normalized_entities


def get_block_state(state: Tag) -> tuple[str, tuple[tuple[str, str], ...]]:
    name = string_value(field(state, "Name"))
    props = field(state, "Properties").value if "Properties" in state.value else {}
    return name, tuple(sorted((key, string_value(value)) for key, value in props.items()))


def map_state(state_key: tuple[str, tuple[tuple[str, str], ...]]) -> tuple[str, tuple[tuple[str, str], ...]]:
    name, props = state_key
    if name == "minecraft:torch":
        return "minecraft:air", ()
    if name == "minecraft:jack_o_lantern":
        return "minecraft:shroomlight", (("axis", "y"),)
    mapping = {
        "minecraft:red_concrete": "minecraft:orange_concrete",
        "minecraft:red_wool": "minecraft:orange_wool",
        "minecraft:gray_concrete": "minecraft:light_gray_concrete",
        "minecraft:brown_stained_glass": "minecraft:light_blue_stained_glass",
        "minecraft:yellow_stained_glass": "minecraft:light_blue_stained_glass",
        "minecraft:yellow_stained_glass_pane": "minecraft:light_blue_stained_glass_pane",
    }
    return mapping.get(name, name), props


def block_state_nbt(state: tuple[str, tuple[tuple[str, str], ...]]) -> Tag:
    name, props = state
    compound = {"Name": nbt_string(name)}
    if props:
        compound["Properties"] = nbt_compound({key: nbt_string(value) for key, value in props})
    return nbt_compound(compound)


def set_int_vector_like(previous: Tag, values: tuple[int, int, int]) -> Tag:
    if previous.type_id == 11:
        return Tag(11, list(values))
    if previous.type_id == 9:
        return nbt_int_list(list(values))
    if previous.type_id == 10:
        keys = ("x", "y", "z")
        result = {}
        for key, value in zip(keys, values):
            prior = previous.value[key]
            result[key] = Tag(prior.type_id, value)
        return nbt_compound(result)
    raise ValueError(f"Cannot update integer vector tag of type {previous.type_id}")


def set_block_entity_position(block_entity: Tag, local_pos: tuple[int, int, int]) -> Tag:
    result = copy.deepcopy(block_entity)
    for key, coordinate in zip(("x", "y", "z"), local_pos):
        previous = result.value.get(key)
        absolute = TRIM_MIN[("x", "y", "z").index(key)] + coordinate
        result.value[key] = Tag(previous.type_id if previous else 3, absolute)
    return result


def coach_interior_state(
        state: tuple[str, tuple[tuple[str, str], ...]],
        local_pos: tuple[int, int, int], style: str) -> tuple[str, tuple[tuple[str, str], ...]]:
    x, y, z = local_pos
    if not (5 <= y <= 14 and 6 <= z <= 16):
        return state
    name, props = state
    if style == "observation":
        replacements = {
            "minecraft:orange_wool": "minecraft:light_blue_wool",
            "minecraft:orange_concrete": "minecraft:light_blue_concrete",
            "minecraft:orange_carpet": "minecraft:light_blue_carpet",
            "minecraft:red_bed": "minecraft:light_blue_bed",
            "minecraft:shroomlight": "minecraft:sea_lantern",
        }
        return replacements.get(name, name), props if name != "minecraft:shroomlight" else ()
    if style == "warm_room":
        replacements = {
            "minecraft:orange_wool": "minecraft:brown_wool",
            "minecraft:orange_concrete": "minecraft:brown_concrete",
            "minecraft:orange_carpet": "minecraft:brown_carpet",
            "minecraft:red_bed": "minecraft:black_bed",
        }
        return replacements.get(name, name), props
    raise ValueError(f"Unknown coach interior style: {style}")


def mirror_state_x(
        state: tuple[str, tuple[tuple[str, str], ...]]) -> tuple[str, tuple[tuple[str, str], ...]]:
    name, props = state
    mirrored: dict[str, str] = {}
    for key, value in props:
        if key == "facing":
            value = {"east": "west", "west": "east"}.get(value, value)
        elif key == "hinge":
            value = {"left": "right", "right": "left"}.get(value, value)
        elif key == "shape":
            value = {
                "inner_left": "inner_right", "inner_right": "inner_left",
                "outer_left": "outer_right", "outer_right": "outer_left",
            }.get(value, value)
        elif key == "rotation":
            value = str((-int(value)) % 16)
        mirrored[key] = value
    for left, right in (("east", "west"),):
        if left in mirrored or right in mirrored:
            left_value, right_value = mirrored.get(left), mirrored.get(right)
            if right_value is not None:
                mirrored[left] = right_value
            else:
                mirrored.pop(left, None)
            if left_value is not None:
                mirrored[right] = left_value
            else:
                mirrored.pop(right, None)
    return name, tuple(sorted(mirrored.items()))


def customize_litematic(source_path: Path, output_path: Path) -> dict[str, Any]:
    validate_added_light_symmetry()
    root_name, root = NbtReader(gzip.decompress(source_path.read_bytes())).root()
    regions = field(root, "Regions").value
    if len(regions) != 1:
        raise ValueError(f"Expected one Litematica region; found {len(regions)}")
    region = next(iter(regions.values()))
    _, source_blocks, source_block_entities, source_entities = read_litematic(
        source_path, expected_size=REFERENCE_SIZE)
    metadata = field(root, "Metadata")
    mapped_source = {pos: map_state(get_block_state(state)) for pos, state in source_blocks.items()}

    expanded: dict[tuple[int, int, int], tuple[str, tuple[tuple[str, str], ...]]] = {
        (x + ORIGINAL_SHIFT, y, z): state
        for (x, y, z), state in mapped_source.items()
    }

    def room_target_pos(source_pos: tuple[int, int, int], target_start: int,
                        reversed_x: bool = False) -> tuple[int, int, int]:
        x, y, z = source_pos
        return room_target_x(x, target_start, reversed_x), y, z

    def copy_room_carriage(target_start: int, style: str, reversed_x: bool = False) -> None:
        for source_x in range(ROOM_SOURCE_START, ROOM_SOURCE_END):
            for y in range(REFERENCE_SIZE[1]):
                for z in range(REFERENCE_SIZE[2]):
                    source_state = mapped_source[(source_x, y, z)]
                    if reversed_x:
                        source_state = mirror_state_x(source_state)
                    target = room_target_pos((source_x, y, z), target_start, reversed_x)
                    expanded[target] = coach_interior_state(source_state, target, style)

    def copy_connector(target_start: int, reversed_x: bool) -> None:
        for source_x in range(TAIL_CONNECTOR_SOURCE_START, TAIL_CONNECTOR_SOURCE_END):
            offset = (TAIL_CONNECTOR_SOURCE_END - 1 - source_x
                      if reversed_x else source_x - TAIL_CONNECTOR_SOURCE_START)
            target_x = target_start + offset
            for y in range(REFERENCE_SIZE[1]):
                for z in range(REFERENCE_SIZE[2]):
                    state = mapped_source[(source_x, y, z)]
                    expanded[(target_x, y, z)] = mirror_state_x(state) if reversed_x else state

    # Keep every original carriage and its transition to the locomotive in
    # place relative to the original train. Append two distinct sleeping/room
    # coaches, with a reference vestibule between them and another at the lounge end.
    copy_room_carriage(ROOM_ONE_START, "observation")
    copy_room_carriage(ROOM_TWO_START, "warm_room", reversed_x=True)
    copy_connector(MID_CONNECTOR_START, reversed_x=False)
    copy_connector(TAIL_CONNECTOR_START, reversed_x=True)

    # The reference sleeper coach's inner end contains a doorway. Close the
    # outside tail with the original lounge coach's finished exterior end face.
    for y in range(REFERENCE_SIZE[1]):
        for z in range(REFERENCE_SIZE[2]):
            expanded[(ROOM_ONE_START, y, z)] = coach_interior_state(
                mapped_source[(0, y, z)], (ROOM_ONE_START, y, z), "observation")
    if any(expanded[pos][0] == "minecraft:air" for pos in TAIL_CAP_INTERIOR_POSITIONS):
        raise ValueError("The copied lounge exterior end face leaves an opening in the new train tail")

    # Open aligned internal passages only. The connector double doors and center
    # posts stay intact; the original second room coach gets a matching one-wide
    # doorway into its preserved head-transition section.
    for pos in ROOM_DOORWAY_AIR_POSITIONS:
        expanded[pos] = ("minecraft:air", ())
    if len(expanded) != math.prod(EXPECTED_SIZE):
        raise ValueError(f"Expanded block volume is incomplete: {len(expanded)} / {math.prod(EXPECTED_SIZE)}")

    original_torches = sum(1 for state in source_blocks.values()
                           if get_block_state(state)[0] == "minecraft:torch")
    replacement_counts: dict[str, int] = {}
    for source_state in source_blocks.values():
        source_name = get_block_state(source_state)[0]
        mapped_name = map_state(get_block_state(source_state))[0]
        if source_name != mapped_name:
            replacement_counts[source_name] = replacement_counts.get(source_name, 0) + 1

    for local_pos in HEAD_CAB_LANTERN_POSITIONS:
        current_state = expanded[local_pos]
        if current_state[0] != "minecraft:air":
            raise ValueError(f"Expected cleared torch/air position for cabin lantern: {local_pos}, found {current_state}")
        support = (local_pos[0], local_pos[1] - 1, local_pos[2])
        if expanded[support][0] != "minecraft:deepslate_tiles":
            raise ValueError(f"Cabin lantern has no verified support at {support}")
        expanded[local_pos] = HEAD_CAB_LANTERN_STATE

    for local_pos in HEAD_INTERIOR_LANTERN_POSITIONS:
        current_state = expanded[local_pos]
        if current_state[0] != "minecraft:air":
            raise ValueError(f"Expected a clear interior light position: {local_pos}, found {current_state}")
        support = (local_pos[0], local_pos[1] + 1, local_pos[2])
        if expanded[support][0] not in ("minecraft:deepslate_tiles", "minecraft:calcite"):
            raise ValueError(f"Interior lantern has no verified overhead support at {support}: {expanded[support]}")
        expanded[local_pos] = HEAD_INTERIOR_LANTERN_STATE

    for local_pos in HEAD_FLOOR_LANTERN_POSITIONS:
        current_state = expanded[local_pos]
        if current_state[0] != "minecraft:air":
            raise ValueError(f"Expected a clear lower control-room light position: {local_pos}, found {current_state}")
        support = (local_pos[0], local_pos[1] - 1, local_pos[2])
        if expanded[support][0] != "minecraft:deepslate_tiles":
            raise ValueError(f"Lower control-room lantern has no verified floor support at {support}: {expanded[support]}")
        expanded[local_pos] = HEAD_FLOOR_LANTERN_STATE

    for local_pos in HEAD_TRANSITION_LANTERN_POSITIONS:
        if expanded[local_pos][0] != "minecraft:air":
            raise ValueError(f"Expected a clear head-transition light position: {local_pos}")
        support = (local_pos[0], local_pos[1] - 1, local_pos[2])
        if expanded[support][0] != "minecraft:deepslate_tiles":
            raise ValueError(f"Head-transition lantern has no verified floor support at {support}")
        expanded[local_pos] = HEAD_TRANSITION_LANTERN_STATE

    for local_pos in HEAD_LOWER_PASSAGE_LIGHT_POSITIONS:
        current_state = expanded[local_pos]
        if current_state[0] != "minecraft:deepslate_tiles":
            raise ValueError(f"Expected a deepslate floor tile for the lower-passage light strip at {local_pos}, found {current_state}")
        expanded[local_pos] = HEAD_LOWER_PASSAGE_LIGHT_STATE

    for local_pos in (*HEAD_INTERIOR_FLOOR_LIGHT_POSITIONS, *HEAD_CRAWLSPACE_FLOOR_LIGHT_POSITIONS):
        if expanded[local_pos][0] != "minecraft:deepslate_tiles":
            raise ValueError(f"Expected a deepslate floor tile for a head interior light at {local_pos}, found {expanded[local_pos]}")
        expanded[local_pos] = HEAD_FLOOR_SHROOMLIGHT_STATE

    for local_pos in TRAIN_DARK_POCKET_SHROOMLIGHT_POSITIONS:
        if expanded[local_pos][0] != "minecraft:deepslate_tiles":
            raise ValueError(f"Expected a deepslate floor tile for a dark-pocket light at {local_pos}, found {expanded[local_pos]}")
        expanded[local_pos] = HEAD_FLOOR_SHROOMLIGHT_STATE

    for local_pos in COACH_A_INTERIOR_LANTERN_POSITIONS:
        if expanded[local_pos][0] != "minecraft:air":
            raise ValueError(f"Expected a clear first-coach upper vestibule light position: {local_pos}, found {expanded[local_pos]}")
        support = (local_pos[0], local_pos[1] + 1, local_pos[2])
        expected_support = "minecraft:white_concrete" if local_pos[1] == 12 else "minecraft:deepslate_tiles"
        if expanded[support][0] != expected_support:
            raise ValueError(f"First-coach lantern has no verified overhead support at {support}")
        expanded[local_pos] = COACH_A_INTERIOR_LANTERN_STATE

    for local_pos in COACH_A_SHROOMLIGHT_POSITIONS:
        current_state = expanded[local_pos]
        expected_base = ("minecraft:deepslate_tiles"
                         if local_pos in COACH_A_SHROOMLIGHT_DEEPSLATE_POSITIONS
                         else "minecraft:light_gray_concrete")
        if current_state[0] != expected_base:
            raise ValueError(f"Unexpected first-coach dark-pocket surface at {local_pos}: {current_state}")
        expanded[local_pos] = COACH_SHROOMLIGHT_STATE

    for local_pos in COACH_UNDERFLOOR_LIGHT_POSITIONS:
        if expanded[local_pos][0] != "minecraft:deepslate_tiles":
            raise ValueError(f"Expected a deepslate tile over the underfloor cavity at {local_pos}, found {expanded[local_pos]}")
        expanded[local_pos] = COACH_SHROOMLIGHT_STATE

    for local_pos in COACH_END_LIGHT_POSITIONS:
        if expanded[local_pos][0] != "minecraft:light_gray_concrete":
            raise ValueError(f"Unexpected coach-end floor at {local_pos}: {expanded[local_pos]}")
        expanded[local_pos] = COACH_SHROOMLIGHT_STATE

    for local_pos in COACH_END_MIRRORED_LIGHT_POSITIONS:
        if expanded[local_pos][0] != "minecraft:air":
            raise ValueError(f"Expected a clear mirrored coach-end light position at {local_pos}, found {expanded[local_pos]}")
        support = (local_pos[0], local_pos[1] - 1, local_pos[2])
        if expanded[support][0] != "minecraft:deepslate_tiles":
            raise ValueError(f"Mirrored coach-end light has no verified floor support at {support}: {expanded[support]}")
        expanded[local_pos] = COACH_SHROOMLIGHT_STATE

    for local_pos in COACH_RAMP_LANTERN_POSITIONS:
        if expanded[local_pos][0] != "minecraft:air":
            raise ValueError(f"Expected a clear coach-ramp lantern position at {local_pos}, found {expanded[local_pos]}")
        support = (local_pos[0], local_pos[1] - 1, local_pos[2])
        if expanded[support][0] not in ("minecraft:orange_wool", "minecraft:light_blue_wool", "minecraft:brown_wool"):
            raise ValueError(f"Coach-ramp lantern has no verified floor support at {support}: {expanded[support]}")
        expanded[local_pos] = COACH_RAMP_LANTERN_STATE

    # Keep original block entities in place and clone the complete sleeping-car
    # furniture into both appended room coaches, including every bed.
    expanded_block_entities: dict[tuple[int, int, int], Tag] = {}

    def add_block_entity(target: tuple[int, int, int], block_entity: Tag) -> None:
        if target in expanded_block_entities:
            raise ValueError(f"Duplicate block entity position after carriage insertion: {target}")
        expanded_block_entities[target] = set_block_entity_position(block_entity, target)

    def remap_source_x(x: int) -> int:
        return x + ORIGINAL_SHIFT

    for (x, y, z), block_entity in source_block_entities.items():
        target_x = remap_source_x(x)
        target = (target_x, y, z)
        add_block_entity(target, block_entity)

    cloned_room_block_entities = [0, 0]
    for source_pos, block_entity in source_block_entities.items():
        source_x, y, z = source_pos
        if not ROOM_SOURCE_START <= source_x < ROOM_SOURCE_END:
            continue
        for index, (destination_start, _) in enumerate(NEW_COACH_RANGES):
            target = room_target_pos(source_pos, destination_start, reversed_x=index == 1)
            add_block_entity(target, block_entity)
            cloned_room_block_entities[index] += 1

    for pos in ROOM_DOORWAY_AIR_POSITIONS:
        if expanded[pos][0] != "minecraft:air" or pos in expanded_block_entities:
            raise ValueError(f"Passage opening is blocked at {pos}")
    for pos in TAIL_CAP_INTERIOR_POSITIONS:
        if expanded[pos][0] == "minecraft:air":
            raise ValueError(f"Train tail cap has an open center cell at {pos}")

    # Coach B contains no display entities. Reject changes to that assumption so
    # a future design cannot silently omit paintings or item frames when cloned.
    if any(90 <= math.floor(pos[0]) < 163 for _, pos in source_entities):
        raise ValueError("Passenger coach template contains an entity that needs explicit cloning")

    source_region_position = int_vector(field(region, "Position"))
    relocated_entities = []
    for entity, old_pos in source_entities:
        new_pos = list(old_pos)
        source_floor_x = math.floor(new_pos[0])
        new_pos[0] = remap_source_x(source_floor_x) + (new_pos[0] - source_floor_x)
        relocated = copy.deepcopy(entity)
        relocated.value["Pos"] = nbt_double_list(new_pos)
        for axis, key in enumerate(("TileX", "TileY", "TileZ")):
            if key not in relocated.value:
                continue
            old_local_tile = (source_region_position[axis] + scalar(relocated.value[key])
                              - TRIM_MIN[axis])
            if axis == 0:
                old_local_tile = remap_source_x(old_local_tile)
            relocated.value[key] = Tag(relocated.value[key].type_id, old_local_tile)
        relocated_entities.append(relocated)

    # Repack all cells in the new positive-origin Litematica region (x fastest,
    # then z, then y), preserving the original region's NBT tag types.
    volume = math.prod(EXPECTED_SIZE)
    palette = sorted(set(expanded.values()) | {("minecraft:air", ())}, key=lambda value: (value[0], value[1]))
    palette_indices = {state: index for index, state in enumerate(palette)}
    bits = max(2, (len(palette) - 1).bit_length())
    packed = [0] * ((volume * bits + 63) // 64)
    index = 0
    for y in range(EXPECTED_SIZE[1]):
        for z in range(EXPECTED_SIZE[2]):
            for x in range(EXPECTED_SIZE[0]):
                state_index = palette_indices[expanded[(x, y, z)]]
                bit_index = index * bits
                word_index, bit_offset = divmod(bit_index, 64)
                packed[word_index] |= (state_index << bit_offset) & ((1 << 64) - 1)
                if bit_offset + bits > 64:
                    packed[word_index + 1] |= state_index >> (64 - bit_offset)
                index += 1
    signed_packed = [value if value < (1 << 63) else value - (1 << 64) for value in packed]

    region.value["Position"] = set_int_vector_like(field(region, "Position"), TRIM_MIN)
    region.value["Size"] = set_int_vector_like(field(region, "Size"), EXPECTED_SIZE)
    region.value["BlockStatePalette"] = nbt_compound_list([block_state_nbt(state) for state in palette])
    region.value["BlockStates"] = Tag(12, signed_packed)
    region.value["TileEntities"] = nbt_compound_list(list(expanded_block_entities.values()))
    region.value["Entities"] = nbt_compound_list(relocated_entities)

    metadata.value["Name"] = nbt_string("星穹列车（1.23.0枢纽定制，四节客车）")
    metadata.value["Description"] = nbt_string(
        "保留原版车头及两节客车，在客厅车厢尾部增接观景卧铺与暖木客房；重做过道、配色并补足照明。"
    )
    if "EnclosingSize" in metadata.value:
        metadata.value["EnclosingSize"] = set_int_vector_like(metadata.value["EnclosingSize"], EXPECTED_SIZE)
    if "TotalVolume" in metadata.value:
        previous = metadata.value["TotalVolume"]
        metadata.value["TotalVolume"] = Tag(previous.type_id, volume)
    if "TotalBlocks" in metadata.value:
        previous = metadata.value["TotalBlocks"]
        metadata.value["TotalBlocks"] = Tag(previous.type_id,
                                             sum(1 for state in expanded.values() if state[0] != "minecraft:air"))
    if "TimeModified" in metadata.value:
        previous = metadata.value["TimeModified"]
        metadata.value["TimeModified"] = Tag(previous.type_id, int(time.time() * 1000))

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_bytes(gzip.compress(write_root(root_name, root), compresslevel=9, mtime=0))

    _, customized_blocks, customized_bes, _ = read_litematic(output_path, expected_size=EXPECTED_SIZE)
    torch_count = sum(1 for state in customized_blocks.values()
                      if get_block_state(state)[0] == "minecraft:torch")
    if torch_count:
        raise ValueError(f"Customized Litematica still has {torch_count} torches")
    for pos in HEAD_CAB_LANTERN_POSITIONS:
        if get_block_state(customized_blocks[pos]) != HEAD_CAB_LANTERN_STATE:
            raise ValueError(f"Customized Litematica is missing cabin lantern at {pos}")
    for pos in HEAD_INTERIOR_LANTERN_POSITIONS:
        if get_block_state(customized_blocks[pos]) != HEAD_INTERIOR_LANTERN_STATE:
            raise ValueError(f"Customized Litematica is missing interior lantern at {pos}")
    for pos in HEAD_FLOOR_LANTERN_POSITIONS:
        if get_block_state(customized_blocks[pos]) != HEAD_FLOOR_LANTERN_STATE:
            raise ValueError(f"Customized Litematica is missing lower control-room lantern at {pos}")
    for pos in HEAD_TRANSITION_LANTERN_POSITIONS:
        if get_block_state(customized_blocks[pos]) != HEAD_TRANSITION_LANTERN_STATE:
            raise ValueError(f"Customized Litematica is missing a head-transition lantern at {pos}")
    for pos in CONNECTOR_LANTERN_POSITIONS:
        if get_block_state(customized_blocks[pos]) != CONNECTOR_LANTERN_STATE:
            raise ValueError(f"Customized Litematica is missing a vestibule lantern at {pos}")
    for pos in CONNECTOR_SHROOMLIGHT_POSITIONS:
        if get_block_state(customized_blocks[pos]) != CONNECTOR_SHROOMLIGHT_STATE:
            raise ValueError(f"Customized Litematica is missing a vestibule shroomlight at {pos}")
    for pos in HEAD_LOWER_PASSAGE_LIGHT_POSITIONS:
        if get_block_state(customized_blocks[pos]) != HEAD_LOWER_PASSAGE_LIGHT_STATE:
            raise ValueError(f"Customized Litematica is missing a lower-passage light at {pos}")
    for pos in (*HEAD_INTERIOR_FLOOR_LIGHT_POSITIONS, *HEAD_CRAWLSPACE_FLOOR_LIGHT_POSITIONS):
        if get_block_state(customized_blocks[pos]) != HEAD_FLOOR_SHROOMLIGHT_STATE:
            raise ValueError(f"Customized Litematica is missing an additional head floor light at {pos}")
    for pos in TRAIN_DARK_POCKET_SHROOMLIGHT_POSITIONS:
        if get_block_state(customized_blocks[pos]) != HEAD_FLOOR_SHROOMLIGHT_STATE:
            raise ValueError(f"Customized Litematica is missing a dark-pocket floor light at {pos}")
    for pos in TAIL_CAP_INTERIOR_POSITIONS:
        if get_block_state(customized_blocks[pos])[0] == "minecraft:air":
            raise ValueError(f"Customized Litematica has an open tail-cap cell at {pos}")
    for pos in COACH_A_INTERIOR_LANTERN_POSITIONS:
        if get_block_state(customized_blocks[pos]) != COACH_A_INTERIOR_LANTERN_STATE:
            raise ValueError(f"Customized Litematica is missing a first-coach lantern at {pos}")
    for pos in COACH_RAMP_LANTERN_POSITIONS:
        if get_block_state(customized_blocks[pos]) != COACH_RAMP_LANTERN_STATE:
            raise ValueError(f"Customized Litematica is missing a coach-ramp lantern at {pos}")
    for pos in COACH_SHROOMLIGHT_POSITIONS:
        if get_block_state(customized_blocks[pos]) != COACH_SHROOMLIGHT_STATE:
            raise ValueError(f"Customized Litematica is missing a coach dark-pocket light at {pos}")
    if len(customized_bes) != len(expanded_block_entities):
        raise ValueError(f"Block entity count changed while writing archive: {len(customized_bes)}")

    coach_interior_counts = {}
    for (name, (start, _)) in zip(("observation", "warm_room"), NEW_COACH_RANGES):
        counts: dict[str, int] = {}
        for x in range(start, start + COACH_LENGTH):
            for y in range(5, 15):
                for z in range(6, 17):
                    state_name = get_block_state(customized_blocks[(x, y, z)])[0]
                    counts[state_name] = counts.get(state_name, 0) + 1
        coach_interior_counts[name] = counts
    if coach_interior_counts["observation"] == coach_interior_counts["warm_room"]:
        raise ValueError("The two new coach interiors are not distinct")
    if cloned_room_block_entities != [29, 29]:
        raise ValueError(f"Unexpected cloned room block-entity counts: {cloned_room_block_entities}")
    source_bed_positions = [pos for pos, state in mapped_source.items()
                            if ROOM_SOURCE_START <= pos[0] < ROOM_SOURCE_END and state[0] == "minecraft:red_bed"]
    if len(source_bed_positions) != 24:
        raise ValueError(f"Unexpected source bed block count in coach template: {len(source_bed_positions)}")
    for source_pos in source_bed_positions:
        observation_pos = room_target_pos(source_pos, NEW_COACH_RANGES[0][0])
        warm_room_pos = room_target_pos(source_pos, NEW_COACH_RANGES[1][0], reversed_x=True)
        if get_block_state(customized_blocks[observation_pos])[0] != "minecraft:light_blue_bed":
            raise ValueError(f"Observation coach is missing a blue bed at {observation_pos}")
        if get_block_state(customized_blocks[warm_room_pos])[0] != "minecraft:black_bed":
            raise ValueError(f"Warm room coach is missing a dark bed at {warm_room_pos}")

    return {
        "source": str(source_path.relative_to(ROOT)).replace("\\", "/"),
        "archive": str(output_path.relative_to(ROOT)).replace("\\", "/"),
        "source_torches_removed": original_torches,
        "replaced_block_counts": replacement_counts,
        "size": list(EXPECTED_SIZE),
        "original_train_x": [ORIGINAL_TRAIN_START, EXPECTED_SIZE[0] - 1],
        "new_coaches": {
            "observation": [ROOM_ONE_START, ROOM_ONE_START + COACH_LENGTH - 1],
            "warm_room": [ROOM_TWO_START, ROOM_TWO_END - 1],
        },
        "connectors": [
            [MID_CONNECTOR_START, MID_CONNECTOR_END - 1],
            [TAIL_CONNECTOR_START, ORIGINAL_TRAIN_START - 1],
        ],
        "tail_cap_source_face": "original lounge carriage x=0 exterior end",
        "passage_air_positions": [list(pos) for pos in ROOM_DOORWAY_AIR_POSITIONS],
        "cloned_room_block_entities": cloned_room_block_entities,
        "head_cabin_lantern_positions": [list(pos) for pos in HEAD_CAB_LANTERN_POSITIONS],
        "head_cabin_lantern_count": len(HEAD_CAB_LANTERN_POSITIONS),
        "head_interior_lantern_positions": [list(pos) for pos in HEAD_INTERIOR_LANTERN_POSITIONS],
        "head_interior_lantern_count": len(HEAD_INTERIOR_LANTERN_POSITIONS),
        "head_floor_lantern_positions": [list(pos) for pos in HEAD_FLOOR_LANTERN_POSITIONS],
        "head_floor_lantern_count": len(HEAD_FLOOR_LANTERN_POSITIONS),
        "head_transition_lantern_positions": [list(pos) for pos in HEAD_TRANSITION_LANTERN_POSITIONS],
        "head_lower_passage_light_positions": [list(pos) for pos in HEAD_LOWER_PASSAGE_LIGHT_POSITIONS],
        "head_lower_passage_light_count": len(HEAD_LOWER_PASSAGE_LIGHT_POSITIONS),
        "head_interior_floor_light_positions": [list(pos) for pos in HEAD_INTERIOR_FLOOR_LIGHT_POSITIONS],
        "head_crawlspace_floor_light_positions": [list(pos) for pos in HEAD_CRAWLSPACE_FLOOR_LIGHT_POSITIONS],
        "dark_pocket_floor_light_positions": [list(pos) for pos in TRAIN_DARK_POCKET_SHROOMLIGHT_POSITIONS],
        "coach_a_interior_lantern_positions": [list(pos) for pos in COACH_A_INTERIOR_LANTERN_POSITIONS],
        "coach_a_shroomlight_positions": [list(pos) for pos in COACH_A_SHROOMLIGHT_POSITIONS],
        "coach_underfloor_light_positions": [list(pos) for pos in COACH_UNDERFLOOR_LIGHT_POSITIONS],
        "coach_end_light_positions": [list(pos) for pos in COACH_END_LIGHT_POSITIONS],
        "coach_end_mirrored_light_positions": [list(pos) for pos in COACH_END_MIRRORED_LIGHT_POSITIONS],
        "coach_ramp_lantern_positions": [list(pos) for pos in COACH_RAMP_LANTERN_POSITIONS],
        "custom_palette_states": len(palette),
        "block_entities": len(expanded_block_entities),
        "entities": len(relocated_entities),
    }


def sanitize_block_entity(block_entity: Tag) -> Tag:
    source = block_entity.value
    identifier = string_value(source["id"])
    # Keep static design data (sign text, patterns, bookshelf slots, and lectern books),
    # but strip placement coordinates, command execution, and runtime world state.
    discarded = {"x", "y", "z", "Command", "LastOutput", "SuccessCount",
                 "TrackOutput", "UpdateLastExecution", "LastExecution",
                 "last_interacted_slot", "Bees", "honey_level", "IsPlaying",
                 "RecordStartTick", "TickCount", "BrewTime", "Fuel"}
    if identifier in ("minecraft:dropper", "minecraft:brewing_stand"):
        discarded.add("Items")
    cleaned = {key: value for key, value in source.items() if key not in discarded}
    if "id" not in cleaned:
        raise ValueError("Block entity is missing its id")
    if identifier == "minecraft:lectern" and "Page" in cleaned:
        cleaned["Page"] = nbt_int(0)
    return nbt_compound(cleaned)


def sanitize_entity(entity: Tag, pos: list[float]) -> Tag:
    source = entity.value
    # StructureTemplate has separate relative `pos` and `blockPos` fields. Runtime
    # coordinates, motion, UUIDs, and portal/status data must not be copied from the save.
    discarded = {"Pos", "UUID", "UUIDMost", "UUIDLeast", "Motion", "Dimension",
                 "TileX", "TileY", "TileZ", "PortalCooldown", "FallDistance",
                 "Air", "Fire", "OnGround", "Invulnerable", "Leash"}
    cleaned = {key: value for key, value in source.items() if key not in discarded}
    if "id" not in cleaned:
        raise ValueError("Entity is missing its id")
    block_pos = [math.floor(x) for x in pos]
    return nbt_compound({
        "pos": nbt_double_list(pos),
        "blockPos": nbt_int_list(block_pos),
        "nbt": nbt_compound(cleaned),
    })


def build_structure() -> tuple[Tag, dict[str, Any]]:
    metadata, source_blocks, source_block_entities, source_entities = read_litematic()
    mapped_blocks = {pos: map_state(get_block_state(state)) for pos, state in source_blocks.items()}
    if mapped_blocks.get(BEACON_LOCAL_POS) != ("minecraft:air", ()):
        raise ValueError(f"Beacon anchor {BEACON_LOCAL_POS} is not an open source cell")
    changed = {"minecraft:torch": 0, "minecraft:jack_o_lantern": 0,
               "minecraft:red_concrete": 0, "minecraft:red_wool": 0,
               "minecraft:gray_concrete": 0, "minecraft:brown_stained_glass": 0,
               "minecraft:yellow_stained_glass": 0,
               "minecraft:yellow_stained_glass_pane": 0}
    for state in source_blocks.values():
        original = get_block_state(state)[0]
        if original in changed:
            changed[original] += 1

    mapped_blocks[BEACON_LOCAL_POS] = ("starrail_sim:star_rail_beacon", ())
    size = EXPECTED_SIZE
    all_states = set(mapped_blocks.values())
    all_states.add(("minecraft:air", ()))
    palette = sorted(all_states, key=lambda value: (value[0], value[1]))
    palette_indices = {state: index for index, state in enumerate(palette)}

    output_blocks = []
    state_histogram: dict[str, int] = {}
    for y in range(size[1]):
        for z in range(size[2]):
            for x in range(size[0]):
                pos = (x, y, z)
                state = mapped_blocks.get(pos, ("minecraft:air", ()))
                state_histogram[state[0]] = state_histogram.get(state[0], 0) + 1
                block = {"pos": nbt_int_list(list(pos)), "state": nbt_int(palette_indices[state])}
                if pos in source_block_entities and state[0] not in ("minecraft:air", "starrail_sim:star_rail_beacon"):
                    block["nbt"] = sanitize_block_entity(source_block_entities[pos])
                output_blocks.append(nbt_compound(block))

    output_entities = [sanitize_entity(entity, pos) for entity, pos in source_entities]
    root = nbt_compound({
        "DataVersion": nbt_int(DATA_VERSION),
        "author": nbt_string("StarRail Sim - customized Litematica reference"),
        "size": nbt_int_list(list(size)),
        "palette": nbt_compound_list([block_state_nbt(state) for state in palette]),
        "blocks": nbt_compound_list(output_blocks),
        "entities": nbt_compound_list(output_entities),
    })
    non_air_count = sum(1 for state in source_blocks.values()
                        if string_value(field(state, "Name")) not in
                        ("minecraft:air", "minecraft:cave_air", "minecraft:void_air"))
    report = {
        "source": str(SOURCE.relative_to(ROOT)).replace("\\", "/"),
        "output": str(OUTPUT.relative_to(ROOT)).replace("\\", "/"),
        "source_author": string_value(field(metadata, "Author")),
        "size": list(size),
        "source_non_air_blocks": non_air_count,
        "template_blocks_including_air": len(output_blocks),
        "palette_states": len(palette),
        "block_entities": len(source_block_entities),
        "entities": len(output_entities),
        "beacon_local_pos": list(BEACON_LOCAL_POS),
        "replacements": changed,
        "output_state_counts": state_histogram,
    }
    return root, report


def verify_output(data: bytes) -> dict[str, Any]:
    validate_added_light_symmetry()
    _, root = NbtReader(gzip.decompress(data)).root()
    if scalar(field(root, "DataVersion")) != DATA_VERSION:
        raise ValueError("Unexpected StructureTemplate DataVersion")
    size = [scalar(x) for x in field(root, "size").value[1]]
    blocks = field(root, "blocks").value[1]
    palette = field(root, "palette").value[1]
    entities = field(root, "entities").value[1]
    if size != list(EXPECTED_SIZE):
        raise ValueError(f"Output size mismatch: {size}")
    if len(blocks) != math.prod(EXPECTED_SIZE):
        raise ValueError(f"Expected every structure cell (including air); found {len(blocks)} blocks")
    state_counts: dict[int, int] = {}
    beacon_count = 0
    cabin_lantern_positions = set()
    interior_lantern_positions = set()
    floor_lantern_positions = set()
    lower_passage_light_positions = set()
    dark_pocket_light_positions = set()
    coach_a_lantern_positions = set()
    coach_ramp_lantern_positions = set()
    coach_shroomlight_positions = set()
    seen_positions = set()
    output_state_by_pos = {}
    for entry in blocks:
        pos = int_vector(field(entry, "pos"))
        if any(pos[i] < 0 or pos[i] >= EXPECTED_SIZE[i] for i in range(3)):
            raise ValueError(f"Output block outside template bounds: {pos}")
        if pos in seen_positions:
            raise ValueError(f"Duplicate output block position: {pos}")
        seen_positions.add(pos)
        state = scalar(field(entry, "state"))
        output_state_by_pos[pos] = state
        state_counts[state] = state_counts.get(state, 0) + 1
        state_name = string_value(field(palette[state], "Name"))
        if state_name == "minecraft:torch":
            raise ValueError(f"StructureTemplate still contains torch at {pos}")
        if state_name == "minecraft:lantern" and pos in HEAD_CAB_LANTERN_POSITIONS:
            cabin_lantern_positions.add(pos)
        if state_name == "minecraft:lantern" and pos in HEAD_INTERIOR_LANTERN_POSITIONS:
            interior_lantern_positions.add(pos)
        if state_name == "minecraft:lantern" and pos in HEAD_FLOOR_LANTERN_POSITIONS:
            floor_lantern_positions.add(pos)
        if state_name == "minecraft:shroomlight" and pos in HEAD_LOWER_PASSAGE_LIGHT_POSITIONS:
            lower_passage_light_positions.add(pos)
        if state_name == "minecraft:shroomlight" and pos in TRAIN_DARK_POCKET_SHROOMLIGHT_POSITIONS:
            dark_pocket_light_positions.add(pos)
        if state_name == "minecraft:lantern" and pos in COACH_A_INTERIOR_LANTERN_POSITIONS:
            coach_a_lantern_positions.add(pos)
        if state_name == "minecraft:lantern" and pos in COACH_RAMP_LANTERN_POSITIONS:
            coach_ramp_lantern_positions.add(pos)
        if state_name == "minecraft:shroomlight" and pos in COACH_SHROOMLIGHT_POSITIONS:
            coach_shroomlight_positions.add(pos)
        if state_name == "starrail_sim:star_rail_beacon" and pos == BEACON_LOCAL_POS:
            beacon_count += 1
        nbt = entry.value.get("nbt")
        if nbt and any(key in nbt.value for key in ("x", "y", "z", "Command")):
            raise ValueError(f"Unclean block entity NBT at {pos}")
    if beacon_count != 1:
        raise ValueError(f"Expected one beacon at {BEACON_LOCAL_POS}; found {beacon_count}")
    if cabin_lantern_positions != set(HEAD_CAB_LANTERN_POSITIONS):
        raise ValueError(f"Cabin lantern positions are incomplete: {sorted(cabin_lantern_positions)}")
    if interior_lantern_positions != set(HEAD_INTERIOR_LANTERN_POSITIONS):
        raise ValueError(f"Interior lantern positions are incomplete: {sorted(interior_lantern_positions)}")
    if floor_lantern_positions != set(HEAD_FLOOR_LANTERN_POSITIONS):
        raise ValueError(f"Lower control-room lantern positions are incomplete: {sorted(floor_lantern_positions)}")
    for pos in HEAD_TRANSITION_LANTERN_POSITIONS:
        if get_block_state(palette[output_state_by_pos[pos]]) != HEAD_TRANSITION_LANTERN_STATE:
            raise ValueError(f"Head-transition lantern is missing at {pos}")
    for pos in CONNECTOR_LANTERN_POSITIONS:
        if get_block_state(palette[output_state_by_pos[pos]]) != CONNECTOR_LANTERN_STATE:
            raise ValueError(f"Vestibule lantern is missing at {pos}")
    for pos in CONNECTOR_SHROOMLIGHT_POSITIONS:
        if get_block_state(palette[output_state_by_pos[pos]]) != CONNECTOR_SHROOMLIGHT_STATE:
            raise ValueError(f"Vestibule shroomlight is missing at {pos}")
    if lower_passage_light_positions != set(HEAD_LOWER_PASSAGE_LIGHT_POSITIONS):
        raise ValueError(f"Lower-passage light positions are incomplete: {sorted(lower_passage_light_positions)}")
    if dark_pocket_light_positions != set(TRAIN_DARK_POCKET_SHROOMLIGHT_POSITIONS):
        raise ValueError(f"Dark-pocket light positions are incomplete: {sorted(dark_pocket_light_positions)}")
    for pos in (*HEAD_INTERIOR_FLOOR_LIGHT_POSITIONS, *HEAD_CRAWLSPACE_FLOOR_LIGHT_POSITIONS):
        if get_block_state(palette[output_state_by_pos[pos]]) != HEAD_FLOOR_SHROOMLIGHT_STATE:
            raise ValueError(f"Additional head floor light is missing at {pos}")
    if coach_a_lantern_positions != set(COACH_A_INTERIOR_LANTERN_POSITIONS):
        raise ValueError(f"First-coach lantern positions are incomplete: {sorted(coach_a_lantern_positions)}")
    if coach_ramp_lantern_positions != set(COACH_RAMP_LANTERN_POSITIONS):
        raise ValueError(f"Coach-ramp lantern positions are incomplete: {sorted(coach_ramp_lantern_positions)}")
    if coach_shroomlight_positions != set(COACH_SHROOMLIGHT_POSITIONS):
        raise ValueError(f"Coach dark-pocket light positions are incomplete: {sorted(coach_shroomlight_positions)}")
    for pos in ROOM_DOORWAY_AIR_POSITIONS:
        state = output_state_by_pos[pos]
        if string_value(field(palette[state], "Name")) != "minecraft:air":
            raise ValueError(f"Passage opening is blocked at {pos}")
    for pos in TAIL_CAP_INTERIOR_POSITIONS:
        state = output_state_by_pos[pos]
        if string_value(field(palette[state], "Name")) == "minecraft:air":
            raise ValueError(f"Train tail cap has an open center cell at {pos}")
    for start, end in NEW_COACH_RANGES:
        bed_count = sum(
            1 for (x, y, z), state_index in output_state_by_pos.items()
            if start <= x < end
            and string_value(field(palette[state_index], "Name")) in
                ("minecraft:light_blue_bed", "minecraft:black_bed")
        )
        if bed_count != 24:
            raise ValueError(f"Expected 24 bed blocks in new coach {start}..{end - 1}; found {bed_count}")

    state_name_by_pos = {
        pos: string_value(field(palette[state_index], "Name"))
        for pos, state_index in output_state_by_pos.items()
    }
    air_positions = {
        pos for pos, name in state_name_by_pos.items()
        if name in ("minecraft:air", "minecraft:cave_air", "minecraft:void_air")
    }
    light_sources = {
        pos for pos, name in state_name_by_pos.items()
        if name in ("minecraft:lantern", "minecraft:sea_lantern", "minecraft:shroomlight", "minecraft:glowstone")
    }
    block_light: dict[tuple[int, int, int], int] = {}
    light_queue = []
    for source in light_sources:
        for axis in range(3):
            for offset in (-1, 1):
                neighbor = list(source)
                neighbor[axis] += offset
                neighbor_pos = tuple(neighbor)
                if neighbor_pos in air_positions and block_light.get(neighbor_pos, 0) < 14:
                    block_light[neighbor_pos] = 14
                    light_queue.append(neighbor_pos)
    queue_index = 0
    while queue_index < len(light_queue):
        current = light_queue[queue_index]
        queue_index += 1
        next_level = block_light[current] - 1
        if next_level <= 0:
            continue
        for axis in range(3):
            for offset in (-1, 1):
                neighbor = list(current)
                neighbor[axis] += offset
                neighbor_pos = tuple(neighbor)
                if neighbor_pos in air_positions and block_light.get(neighbor_pos, 0) < next_level:
                    block_light[neighbor_pos] = next_level
                    light_queue.append(neighbor_pos)

    spawnable_full_blocks = {
        "minecraft:deepslate_tiles", "minecraft:light_gray_concrete", "minecraft:calcite",
        "minecraft:smooth_sandstone", "minecraft:white_concrete", "minecraft:black_concrete",
        "minecraft:green_concrete", "minecraft:dark_oak_planks", "minecraft:spruce_planks",
        "minecraft:stripped_oak_log", "minecraft:jukebox", "minecraft:crafting_table",
        "minecraft:dropper", "minecraft:grass_block", "minecraft:orange_concrete",
        "minecraft:brown_concrete", "minecraft:light_blue_concrete",
    }
    spawn_candidates = []
    for x in range(EXPECTED_SIZE[0]):
        for y in range(2, 17):
            for z in range(1, EXPECTED_SIZE[2] - 1):
                pos = (x, y, z)
                support_name = state_name_by_pos[(x, y - 1, z)]
                if (pos in air_positions and (x, y + 1, z) in air_positions
                        and support_name in spawnable_full_blocks):
                    spawn_candidates.append(pos)
    unlit_candidates = [pos for pos in spawn_candidates if block_light.get(pos, 0) == 0]
    if unlit_candidates:
        raise ValueError(f"Train interior has {len(unlit_candidates)} dark spawn candidates: {unlit_candidates[:12]}")

    for entity in entities:
        nbt = field(entity, "nbt")
        if any(key in nbt.value for key in ("UUID", "UUIDMost", "UUIDLeast", "Pos", "Motion", "Dimension")):
            raise ValueError("Entity template carries stale world or runtime data")
        entity_pos = [scalar(x) for x in field(entity, "pos").value[1]]
        if any(entity_pos[i] < 0 or entity_pos[i] >= EXPECTED_SIZE[i] for i in range(3)):
            raise ValueError(f"Output entity outside template bounds: {entity_pos}")
    return {"size": size, "blocks": len(blocks), "palette": len(palette),
            "block_entities": sum(1 for b in blocks if "nbt" in b.value), "entities": len(entities),
            "beacon": beacon_count,
            "head_cabin_lanterns": len(cabin_lantern_positions),
            "head_interior_lanterns": len(interior_lantern_positions),
            "head_floor_lanterns": len(floor_lantern_positions),
            "head_transition_lanterns": len(HEAD_TRANSITION_LANTERN_POSITIONS),
            "head_interior_floor_lights": len(HEAD_INTERIOR_FLOOR_LIGHT_POSITIONS),
            "head_crawlspace_floor_lights": len(HEAD_CRAWLSPACE_FLOOR_LIGHT_POSITIONS),
            "dark_pocket_floor_lights": len(dark_pocket_light_positions),
            "connector_lanterns": len(CONNECTOR_LANTERN_POSITIONS),
            "connector_shroomlights": len(CONNECTOR_SHROOMLIGHT_POSITIONS),
            "head_lower_passage_lights": len(lower_passage_light_positions),
            "coach_a_interior_lanterns": len(coach_a_lantern_positions),
            "coach_ramp_lanterns": len(coach_ramp_lantern_positions),
            "coach_shroomlights": len(coach_shroomlight_positions),
            "new_coach_bed_blocks_each": 24,
            "passage_openings": len(ROOM_DOORWAY_AIR_POSITIONS),
            "tail_cap_center_cells": len(TAIL_CAP_INTERIOR_POSITIONS),
            "interior_spawn_candidates": len(spawn_candidates),
            "unlit_interior_spawn_candidates": len(unlit_candidates)}


def main() -> None:
    global SOURCE, OUTPUT
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, default=REFERENCE_SOURCE,
                        help="unmodified reference .litematic file")
    parser.add_argument("--archive", type=Path, default=CUSTOMIZED_SOURCE,
                        help="destination for the customized .litematic archive")
    parser.add_argument("--output", type=Path, default=OUTPUT, help="destination structure .nbt")
    args = parser.parse_args()
    customization = customize_litematic(args.source.resolve(), args.archive.resolve())
    SOURCE, OUTPUT = args.archive.resolve(), args.output.resolve()
    structure, report = build_structure()
    report["litematic_customization"] = customization
    raw = write_root("", structure)
    compressed = gzip.compress(raw, compresslevel=9, mtime=0)
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_bytes(compressed)
    report["output_bytes"] = len(compressed)
    report["static_validation"] = verify_output(compressed)
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
