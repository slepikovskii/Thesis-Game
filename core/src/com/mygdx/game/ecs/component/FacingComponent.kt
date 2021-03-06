package com.mygdx.game.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class FacingComponent : Component, Pool.Poolable {
    var direction = FacingDirection.RIGHT

    override fun reset() {
        direction = FacingDirection.RIGHT
    }

    companion object {
        val mapper = mapperFor<FacingComponent>()
    }
}

enum class FacingDirection {
    LEFT, RIGHT
}
