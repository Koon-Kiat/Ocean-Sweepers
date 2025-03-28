package project.game.common.config.api;

import project.game.engine.constant.api.IDynamicConstants;

/**
 * Top-level interface for game constants.
 * 
 * This provides a central point of reference for all constant categories.
 */
public interface IGameConstants extends

                IDynamicConstants,
                IPhysicsConstants,
                IScreenConstants,
                IEntityConstants,
                IMovementConstants {
}