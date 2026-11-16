/**
 * Domain: model (entities, movement and combat rules).
 * Application: command queue, commands, control strategies and tank spawning.
 * Infrastructure: desktop startup, keyboard input, level sources and graphics.
 *
 * LevelSource and WorldListener are output ports; file/random loaders and
 * RenderSystem implement them. Runnable commands are the input port for actions.
 * Field, TankState and ControlStrategy are internal contracts, not I/O ports.
 * EntityRenderer is a graphics contract, not a domain port.
 *
 * TankGame wires the application to LibGDX. For a fully isolated application
 * layer, its frame loop would need a game-session input port and a rendering
 * output port; currently that orchestration still lives in the desktop adapter.
 * Keyboard access already enters through IntPredicate callbacks.
 */
package ru.mipt.bit.platformer;
