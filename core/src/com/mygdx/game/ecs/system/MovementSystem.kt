package com.mygdx.game.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.MathUtils
import com.mygdx.game.ecs.component.FacingComponent
import com.mygdx.game.ecs.component.FacingDirection
import com.mygdx.game.ecs.component.MoveComponent
import com.mygdx.game.ecs.component.TransformComponent
import com.mygdx.game.event.GameEvent
import com.mygdx.game.event.GameEventManager
import ktx.ashley.allOf
import ktx.ashley.get

private const val HOR_ACCELERATION = 50f

class MoveSystem(private val gameEventManager: GameEventManager) : IteratingSystem(
        allOf(TransformComponent::class, MoveComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val moveCmp = entity[MoveComponent.mapper]
        require(moveCmp != null) { "Entity |entity| must have an MoveComponent. entity=$entity" }

        when {
            Gdx.input.isKeyPressed(Input.Keys.RIGHT) -> {
                moveCmp.speed = moveCmp.speed + HOR_ACCELERATION * deltaTime
                entity[FacingComponent.mapper]?.direction = FacingDirection.RIGHT
                entity[TransformComponent.mapper]?.let {
                    it.position.x = MathUtils.clamp(
                            it.position.x + moveCmp.speed * deltaTime,
                            -40f,
                            Gdx.graphics.width + 40 - it.size.x
                    )
                }
                gameEventManager.dispatchEvent(GameEvent.PlayerMoved(FacingDirection.RIGHT))
            }
            Gdx.input.isKeyPressed(Input.Keys.LEFT) -> {
                moveCmp.speed = moveCmp.speed + HOR_ACCELERATION * deltaTime
                entity[FacingComponent.mapper]?.direction = FacingDirection.LEFT
                entity[TransformComponent.mapper]?.let {
                    it.position.x = MathUtils.clamp(
                            it.position.x - moveCmp.speed * deltaTime,
                            -40f,
                            Gdx.graphics.width + 40 - it.size.x
                    )
                }
                gameEventManager.dispatchEvent(GameEvent.PlayerMoved(FacingDirection.LEFT))
            }
            else -> {
                moveCmp.speed = 0f
                gameEventManager.dispatchEvent(GameEvent.PlayerMoved(null))
            }
        }
    }
}