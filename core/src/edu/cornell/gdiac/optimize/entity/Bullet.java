/*
 * Bullet.java
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.optimize.entity;

import edu.cornell.gdiac.optimize.*;

/**
 * Model class for bullets fired by the ship.
 */
public class Bullet extends GameObject {
	/**
	 * Returns the type of this object.
	 *
	 * We use this instead of runtime-typing for performance reasons.
	 *
	 * @return the type of this object.
	 */
	public ObjectType getType() {
		return ObjectType.BULLET;
	}

	/**
	 * Indicates which ship this bullet came from
	 */
	public Ship s;
	/**
	 * Initialize bullet with trivial starting position.
	 */
	public Bullet() {
	}
	public Bullet(Ship k){
		s = k;
	}
}