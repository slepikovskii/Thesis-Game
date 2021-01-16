package com.mygdx.game.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.utils.GdxRuntimeException
import com.mygdx.game.assests.Animations
import com.mygdx.game.ecs.component.Animation2D
import com.mygdx.game.ecs.component.AnimationComponent
import com.mygdx.game.ecs.component.AnimationType
import com.mygdx.game.ecs.component.GraphicComponent
import com.mygdx.game.event.GameEvent
import com.mygdx.game.event.GameEventListener
import com.mygdx.game.event.GameEventManager
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.assets.async.AssetStorage
import ktx.log.debug
import ktx.log.error
import ktx.log.logger
import java.util.*

private val LOG = logger<AnimationSystem>()

class AnimationSystem(
        private val assetStorage: AssetStorage,
        private val eventManager: GameEventManager
) : IteratingSystem(
        allOf(AnimationComponent::class, GraphicComponent::class).get()), EntityListener, GameEventListener {
    private val animationCache = EnumMap<AnimationType, Animation2D>(AnimationType::class.java)
    private var isMoving = false

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        engine.addEntityListener(family, this)
        eventManager.addListener(GameEvent.PlayerMoved::class, this)
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        engine.removeEntityListener(this)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val aniCmp = entity[AnimationComponent.mapper]
        require(aniCmp != null) { "Entity |entity| must have an AnimationComponent. entity=$entity" }
        val graphic = entity[GraphicComponent.mapper]
        require(graphic != null) { "Entity |entity| must have a GraphicComponent. entity=$entity" }

        if (aniCmp.type == AnimationType.NONE) {
            LOG.error { "No aniCmp type specified" }
            return
        }

        if (aniCmp.animation.type == aniCmp.type) {
            if (isMoving) {
                aniCmp.stateTime += deltaTime
            } else {
                aniCmp.stateTime = 0f
            }
        } else {
            // change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAnimation(aniCmp.type)
        }

        val frame = aniCmp.animation.getKeyFrame(aniCmp.stateTime)
        graphic.setSpriteRegion(frame)
    }

    private fun getAnimation(type: AnimationType): Animation2D {
        var animation = animationCache[type]
        if (animation == null) {
            val regions = assetStorage[Animations.byName(type.assetName).descriptor].regions
            if (regions.isEmpty) {
                throw GdxRuntimeException("There is no animation region in the game atlas")
            } else {
                LOG.debug { "Adding animation of type $type with ${regions.size} regions" }
            }
            animation = Animation2D(type, regions, type.playMode, type.speed)
            animationCache[type] = animation
        }
        return animation
    }

    override fun entityRemoved(entity: Entity?) = Unit

    override fun entityAdded(entity: Entity) {
        entity[AnimationComponent.mapper]?.let { aniCmp ->
            aniCmp.animation = getAnimation(aniCmp.type)
            val frame = aniCmp.animation.getKeyFrame(aniCmp.stateTime)
            entity[GraphicComponent.mapper]?.setSpriteRegion(frame)
        }
    }

    override fun onEvent(event: GameEvent) {
        if (event is GameEvent.PlayerMoved) {
            isMoving = event.speed != 0f
        }
    }
}
