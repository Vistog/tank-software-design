from dataclasses import dataclass, field as dataclass_field
from enum import Enum
from math import isfinite
from types import MappingProxyType
from typing import Any


@dataclass(frozen=True)
class Cell:
    x: int
    y: int


class Direction(Enum):
    UP = (0, 1, 90.0)
    LEFT = (-1, 0, 180.0)
    DOWN = (0, -1, -90.0)
    RIGHT = (1, 0, 0.0)

    @property
    def rotation_degrees(self) -> float:
        return self.value[2]

    def next_cell(self, cell: Cell) -> Cell:
        dx, dy, _ = self.value
        return Cell(cell.x + dx, cell.y + dy)


@dataclass(frozen=True)
class Entity:
    id: int
    blocks_movement: bool


class Tank(Entity):
    def __init__(self, entity_id: int):
        super().__init__(entity_id, blocks_movement=True)


class Tree(Entity):
    def __init__(self, entity_id: int):
        super().__init__(entity_id, blocks_movement=True)


class Field:
    def contains(self, cell: Cell) -> bool:
        raise NotImplementedError


class RectangleField(Field):
    def __init__(self, width: int, height: int):
        if width <= 0 or height <= 0:
            raise ValueError("field dimensions must be positive")
        self.width = width
        self.height = height

    def contains(self, cell: Cell) -> bool:
        return 0 <= cell.x < self.width and 0 <= cell.y < self.height


@dataclass
class Movement:
    # Начальная клетка остаётся в EntityState.cell до завершения движения.
    # Параметры проверяет менеджер перед созданием Movement.
    destination: Cell
    duration: float
    elapsed: float = dataclass_field(default=0.0, init=False)

    def get_progress(self) -> float:
        return min(1.0, self.elapsed / self.duration)


@dataclass
class EntityState:
    # Одна запись мира: фишка, размещение и необязательный переход.
    entity: Entity
    cell: Cell
    facing: Direction
    movement: Movement | None = dataclass_field(default=None, init=False)


class GameWorld:
    def __init__(self, field: Field):
        if field is None:
            raise ValueError("field is required")
        self.field = field
        self.states: dict[int, EntityState] = {}

    def add(self, entity: Entity, cell: Cell, facing: Direction) -> None:
        if entity is None or cell is None or facing is None:
            raise ValueError("entity, cell and facing are required")
        if entity.id in self.states or not self.field.contains(cell):
            raise ValueError("duplicate id or position outside the field")
        if entity.blocks_movement and not self.is_free_for_movement(cell):
            raise ValueError("cell is occupied or reserved")
        self.states[entity.id] = EntityState(entity, cell, facing)

    def get_state(self, entity_id: int) -> EntityState:
        # В Java private: нужен только менеджеру и рендереру.
        if entity_id not in self.states:
            raise ValueError(f"unknown entity: {entity_id}")
        return self.states[entity_id]

    def is_free_for_movement(self, cell: Cell) -> bool:
        if not self.field.contains(cell):
            return False
        for state in self.states.values():
            if state.entity.blocks_movement and state.cell == cell:
                return False
            # Цель текущего перехода и есть резерв. Отдельной таблицы нет.
            if state.movement is not None and state.movement.destination == cell:
                return False
        return True


class MovementManager:
    def try_start_move(
        self, world: GameWorld, entity_id: int, direction: Direction, duration: float
    ) -> bool:
        if not isfinite(duration) or duration <= 0:
            raise ValueError("movement duration must be finite and positive")
        if direction is None:
            raise ValueError("direction is required")
        state = world.get_state(entity_id)
        if state.movement is not None:
            return False

        state.facing = direction
        destination = direction.next_cell(state.cell)
        if not world.is_free_for_movement(destination):
            return False
        state.movement = Movement(destination, duration)
        return True

    def update(self, world: GameWorld, delta_time: float) -> None:
        if not isfinite(delta_time) or delta_time < 0:
            raise ValueError("delta_time must be finite and non-negative")
        for state in world.states.values():
            movement = state.movement
            if movement is None:
                continue
            movement.elapsed = min(movement.duration, movement.elapsed + delta_time)
            if movement.get_progress() == 1.0:
                state.cell = movement.destination
                state.movement = None


class RenderSystem:
    def __init__(self, tile_layer, backend):
        self.backend = backend
        self.tile_movement = backend.create_tile_movement(tile_layer, interpolation="smooth")
        self.renderables: dict[int, Any] = {}  # ID -> TextureRegion

    def register(self, entity_id: int, graphics) -> None:
        self.renderables[entity_id] = graphics

    def render(self, batch, world: GameWorld) -> None:
        for entity_id, graphics in self.renderables.items():
            state = world.get_state(entity_id)
            movement = state.movement
            destination = state.cell if movement is None else movement.destination
            progress = 1.0 if movement is None else movement.get_progress()
            rectangle = self.backend.create_bounding_rectangle(graphics)
            # TileMovement переводит клетки в экранные координаты и применяет smooth.
            self.tile_movement.move_rectangle_between_tile_centers(
                rectangle, state.cell, destination, progress
            )
            self.backend.draw_texture_region_unscaled(
                batch, graphics, rectangle, state.facing.rotation_degrees
            )


class GameDesktopLauncher:
    TANK_MOVEMENT_DURATION_SECONDS = 0.4
    PLAYER_TANK_ID = 1
    TREE_ID = 2
    # Строки заменяют Input.Keys. Порядок такой же, как у LinkedHashMap в Java.
    KEY_BINDINGS = MappingProxyType({
        "UP": Direction.UP, "W": Direction.UP,
        "LEFT": Direction.LEFT, "A": Direction.LEFT,
        "DOWN": Direction.DOWN, "S": Direction.DOWN,
        "RIGHT": Direction.RIGHT, "D": Direction.RIGHT,
    })

    def __init__(self, backend):
        self.backend = backend
        self.movement_manager = MovementManager()

    def create(self) -> None:
        self.batch = self.backend.create_sprite_batch()
        self.level = self.backend.load_tmx("level.tmx")
        self.level_renderer = self.backend.create_single_layer_map_renderer(self.level, self.batch)
        ground_layer = self.backend.get_single_layer(self.level)

        self.world = GameWorld(RectangleField(ground_layer.width, ground_layer.height))
        self.render_system = RenderSystem(ground_layer, self.backend)
        self.world.add(Tank(self.PLAYER_TANK_ID), Cell(1, 1), Direction.RIGHT)
        self.world.add(Tree(self.TREE_ID), Cell(1, 3), Direction.RIGHT)

        self.blue_tank_texture = self.backend.load_texture("images/tank_blue.png")
        self.green_tree_texture = self.backend.load_texture("images/greenTree.png")
        self.render_system.register(
            self.PLAYER_TANK_ID, self.backend.texture_region(self.blue_tank_texture)
        )
        self.render_system.register(
            self.TREE_ID, self.backend.texture_region(self.green_tree_texture)
        )

    def render(self) -> None:
        self.backend.clear_screen(0, 0, 0.2, 1)
        requested_direction = self.pressed_direction()
        if requested_direction is not None:
            self.movement_manager.try_start_move(
                self.world, self.PLAYER_TANK_ID, requested_direction,
                self.TANK_MOVEMENT_DURATION_SECONDS,
            )
        self.movement_manager.update(self.world, self.backend.get_delta_time())

        self.level_renderer.render()
        self.batch.begin()
        self.render_system.render(self.batch, self.world)
        self.batch.end()

    def pressed_direction(self) -> Direction | None:
        for key, direction in self.KEY_BINDINGS.items():
            if self.backend.is_key_pressed(key):
                return direction
        return None


    def dispose(self) -> None:
        self.green_tree_texture.dispose()
        self.blue_tank_texture.dispose()
        self.level.dispose()
        self.batch.dispose()

    @staticmethod
    def main(backend) -> None:
        backend.run_application(GameDesktopLauncher(backend), width=1280, height=1024)
