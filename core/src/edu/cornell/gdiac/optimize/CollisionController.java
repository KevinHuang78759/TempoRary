/*
 * CollisionController.java
 *
 * This controller implements basic collision detection as described in
 * the instructions.  All objects in this game are treated as circles,
 * and a collision happens when circles intersect.
 *
 * This controller is EXTREMELY ineffecient.  To improve its performance,
 * you will need to use collision cells, as described in the instructions.
 * You should not need to modify any method other than the constructor
 * and processCollisions.  However, you will need to add your own methods.
 *
 * This is the only file that you need to modify as the first part of
 * the lab. 
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.optimize;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.math.*;
import edu.cornell.gdiac.optimize.entity.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Controller implementing simple game physics.
 *
 * This is a very inefficient physics engine.  Part of this lab is determining
 * how to make it more efficient.
 */
public class CollisionController {

	// 'Bounciness' constants
	/** Restitution for colliding with the (hard coded) box */
	protected static final float BOX_COEFF_REST   = 0.95f;
	/** Restitution for colliding with the (hard coded) bump */
	protected static final float BUMP_COEFF_REST  = 1.95f;
	/** Dampening factor when colliding with floor or shell */
	protected static final float DAMPENING_FACTOR = 0.95f;
	
	// Geometry of the background image
	/** (Scaled) distance of the floor ledge from bottom */
	protected static final float BOTTOM_OFFSET    = 0.075f;
	/** (Scaled) position of the box center */
	protected static final float BOX_X_POSITION   = 0.141f;
	/** (Scaled) position of half the box width */
	protected static final float BOX_HALF_WIDTH   = 0.133f;
	/** (Scaled) position of the box height from bottom of screen */
	protected static final float BOX_FULL_HEIGHT  = 0.2f;
	/** (Scaled) position of the bump center */
	protected static final float BUMP_X_POSITION  = 0.734f;
	/** (Scaled) position of the bump radius */
	protected static final float BUMP_RADIUS      = 0.11f;
	
	// These cannot be modified after the controller is constructed.
	// If these change, make a new constructor.
	/** Width of the collision geometry */
	private float width;
	/** Height of the collision geometry */
	private float height;
	
	// Cache objects for collision calculations
	private Vector2 temp1;
	private Vector2 temp2;

	/// ACCESSORS
	
	/**
	 * Returns width of the game window (necessary to detect out of bounds)
	 *
	 * @return width of the game window
	 */
	public float getWidth() {
		return width;
	}
	
	/**
	 * Returns height of the game window (necessary to detect out of bounds)
	 *
	 * @return height of the game window
	 */
	public float getHeight() {
		return height;
	}
	
	/**
	 * Returns the height of the floor ledge.
	 *
	 * The floor ledge supports the player ship, and is what all of the shells
	 * bounce off of.  It is raised slightly higher than the bottom of the screen.
	 *
	 * @return the height of the floor ledge.
	 */
	public float getFloorLedge() {
		return BOTTOM_OFFSET*height;
	}
	
	/**
	 * Returns x-coordinate of the center of the square box in the background image.
	 *
	 * @return x-coordinate of the center of the square bo
	 */
	public float getBoxX() {
		return BOX_X_POSITION * width;
	}
	
	/**
	 * Returns half of the width of the square box in the background image.
	 *
	 * The box edges are x+/- this width.
	 *
	 * @return half of the width of the square box
	 */
	public float getBoxRadius() {
		return BOX_HALF_WIDTH * width;
	}
	
	/**
	 * Returns height of the square box in the background image
	 *
	 * Height is measured from the bottom of the screen, not the ledge.
	 */
	public float getBoxHeight() {
		return BOX_FULL_HEIGHT * height;
	}
	
	/**
	 * Returns x-coordinate of the center of the semicircular bump in the background image
	 *
	 * @return x-coordinate of the center of the semicircular bump
	 */
	protected float getBumpX() {
		return BUMP_X_POSITION * width;
	}
	
	/**
	 * Returns radius of the semicircular bump in the background image
	 *
	 * @return radius of the semicircular bump
	 */
	protected float getBumpRadius() {
		return 0.11f * width;
	}
	
	//#region Initialization (MODIFY THIS CODE)
	/**
	 * The limit on the number of shells per cell
	 */
	int cell_capacity;

	/**
	 * How close shells  must be to each other to be considered under the same hash <br>
	 * Effectively this is the size of each "cell"
	 */
	float hash_tolerance;

	HashMap<Integer, ArrayList<GameObject>> nearest;
	/**
	 * Creates a CollisionController for the given screen dimensions.
	 *
	 * @param width   Width of the screen 
	 * @param height  Height of the screen
	 * @param collision_tolerance width of cell, INVARIANT: THIS MUST BE <br>
	 *                            GREATER THAN OR EQUAL TO THE RADIUS OF THE LARGEST OBJECT <br>
	 *                            IN ORDER FOR CORRECTNESS TO HOLD
	 */
	public CollisionController(float width, float height, float collision_tolerance/*, int cap*/) {
		this.width = width;
		this.height = height;
		// Initialize cache objects
		temp1 = new Vector2();
		temp2 = new Vector2();

		hash_tolerance = collision_tolerance;
		//cell_capacity = cap;
	}
	
	/**
	 * This is the main (incredibly unoptimized) collision detetection method.
	 *
	 * @param objects List of live objects to check 
	 * @param offset  Offset of the box and bump 
	 */
	public void processCollisions(Array<GameObject> objects, int offset) {

		for(GameObject o : objects){
			processBounds(o, offset);
		}
		//Empty our cells
//		nearest = new HashMap<Integer, ArrayList<GameObject>>();
//
//		//Create an empty dividing tree or whatever this thing from CS 4320 was called
//		//CellTree ct = new CellTree(getWidth(), getHeight(), 0f, 0f, Math.max(objects.size/100, cell_capacity));
//
//
//		// For each shell, check for collisions with the special terrain elements
//		// Also put the shells into cells
//		for (GameObject o : objects) {
//			//Add this object to the tree
//			//ct.add(o);
//
//			//If this object has been destroyed, we don't need to check collisions.
//			if(o.destroyed){
//				continue;
//			}
//
//			//Calculate what cell this object is in
//			float tx = o.getX();
//			float ty = o.getY();
//
//			int x = (int) (tx/hash_tolerance);
//			int y = (int) (ty/hash_tolerance);
//			// Make sure object is in bounds.
//			// For shells, this handles the box and bump.
//			if(tx < 50 || ty < 250 || tx >getWidth() - 50){
//
//				processBounds(o, offset);
//
//			}
//
//			//Check to see if the shell is in the frame of gameplay - if it is not
//			//a TA has indicated on ed post #155 that we do not need to check for object-object collisions
//			//outside the visible boundaries. Notice that since we process the boundaries first, the objects
//			//will always be in bounds on the x-coordinate
//			if(ty > height){
//				continue;
//			}
//
//			//Calculate a cell key; since our x and y coordinates are limited we can just mulitply one of them
//			//by a large number and add them up
//			int key = 100000*x + y;
//			//Check to see if there are elements in this cell
//			if(nearest.containsKey(key)){
//				ArrayList<GameObject> tt = nearest.get(key);
//				for(GameObject k : tt){
//					processCollision(o, k);
//
//				}
//			}
//			//Calculate the relative location of the object to the bottom left of the cell
//			float dx = tx - x*hash_tolerance;
//			float dy = ty - y*hash_tolerance;
//
//			//Check which cells this object overlaps with and calculate collisions
//			if(dx + o.radius > hash_tolerance){
//				//right cell
//				int dkey = key + 100000;
//				if(nearest.containsKey(dkey)){
//					ArrayList<GameObject> tt = nearest.get(dkey);
//					for(GameObject k : tt){
//						processCollision(o, k);
//
//					}
//				}
//				//top right cell
//				if(dy + o.radius > hash_tolerance){
//					int ukey = dkey + 1;
//					if(nearest.containsKey(ukey)){
//						ArrayList<GameObject> tt = nearest.get(ukey);
//						for(GameObject k : tt){
//							processCollision(o, k);
//						}
//					}
//				}
//				//bottom right cell
//				if(dy < o.radius){
//					int ukey = dkey - 1;
//					if(nearest.containsKey(ukey)){
//						ArrayList<GameObject> tt = nearest.get(ukey);
//						for(GameObject k : tt){
//							processCollision(o, k);
//						}
//					}
//				}
//			}
//
//			if(dx < o.radius){
//				//left cell
//				int dkey = key - 100000;
//				if(nearest.containsKey(dkey)){
//					ArrayList<GameObject> tt = nearest.get(dkey);
//					for(GameObject k : tt){
//						processCollision(o, k);
//					}
//				}
//				//top left cell
//				if(dy + o.radius > hash_tolerance){
//					int ukey = dkey + 1;
//					if(nearest.containsKey(ukey)){
//						ArrayList<GameObject> tt = nearest.get(ukey);
//						for(GameObject k : tt){
//							processCollision(o, k);
//						}
//					}
//				}
//				//bottom left cell
//				if(dy < o.radius){
//					int ukey = dkey - 1;
//					if(nearest.containsKey(ukey)){
//						ArrayList<GameObject> tt = nearest.get(ukey);
//						for(GameObject k : tt){
//							processCollision(o, k);
//						}
//					}
//				}
//
//			}
			//top cell
//			if(dy + o.radius > hash_tolerance){
//				int ukey = key + 1;
//				if(nearest.containsKey(ukey)){
//					ArrayList<GameObject> tt = nearest.get(ukey);
//					for(GameObject k : tt){
//						processCollision(o, k);
//					}
//				}
//			}
			//bottom cell
//			if(dy < o.radius){
//				int ukey = key - 1;
//				if(nearest.containsKey(ukey)){
//					ArrayList<GameObject> tt = nearest.get(ukey);
//					for(GameObject k : tt){
//						processCollision(o, k);
//					}
//				}
//			}



//			for(int i = -1; i <= 1; ++i){
//				for(int j = -1; j <= 1; ++j){
//					int ckey = key + 100000*i + j;
//					if(nearest.containsKey(ckey)){
//						ArrayList<GameObject> tt = nearest.get(ckey);
//						for(GameObject k : tt){
//							processCollision(o, k);
//
//						}
//					}
//				}
//			}
			//If this cell has been initialized, add the object to the cell
			//Otherwise, initialize the cell and add the object to the cell
//			if(nearest.containsKey(key)){
//				nearest.get(key).add(o);
//			}
//			else{
//				ArrayList<GameObject> list = new ArrayList<>();
//				list.add(o);
//				nearest.put(key, list);
//			}


			//#region REPLACE THIS CODE
			/* This is the slow code that must be replaced. */

//			for (int ii = 0; ii < objects.size; ii++) {
//				if (objects.get(ii) != o) {
//					processCollision(o,objects.get(ii));
//				}
//			}
			//#endregion
		}



	//}


	//#endregion
	
	//#region Cell Management (INSERT CODE HERE)
//	static class CellTree{
//		/**
//	 	* Objects in this cell-node
//	 	*/
//		ArrayList<GameObject> objects;
//		/**
//	 	* Whether or not this node has been split
//	 	*/
//		boolean split;
//		/**
//	 	* Child nodes representing the 4 quadrants of this cell
//	 	*/
//		CellTree q1, q2, q3,q4;
//		/**
//	 	* Dimensions and locations of this cell node
//	 	*/
//		float w, h, blx, bly;
//		/**
//	 	* Capacity of this cell
//	 	*/
//		int cap;
//		public CellTree(float width, float height, float blx, float bly,  int c){
//			w = width;
//			h = height;
//			this.blx = blx;
//			this.bly = bly;
//			objects = new ArrayList<>();
//			split = false;
//			cap = c;
//		}
//		/**
//	 	* Adds an object to the tree
//	    * @return whether or not the object was actually added
//	 	*/
//		public boolean add(GameObject k){
//			float x = k.getX();
//			float y = k.getY();
//			float rad = k.radius;
//	        //Check if this object is within the bounds of the tree
//			if(x - rad > blx + w || y - rad > bly + h || x + rad < blx || y + rad < bly){
//				return false;
//			}
//          //if there is space, add the object
//			if(objects.size() < cap){
//				objects.add(k);
//				return true;
//			}
//          //If there is no space, and this is a leaf node, we need to split it into quadrants
//			if(!split){
//				splitting();
//			}
//          //Traverse and add the object to a child node
//			if(q1.add(k)){
//				return true;
//			}
//			if(q2.add(k)){
//				return true;
//			}
//			if(q3.add(k)){
//				return true;
//			}
//			return q4.add(k);
//		}
//
//		/**
//	 	 * Split the node into child nodes
//	 	 */
//		private void splitting(){
//          //Determine a new cap that is smaller, but bound it so it doesn't get too small
//			int newcap = Math.max(cap/4, 10);
//          //Initialize quadrant child nodes
//			q1 = new CellTree(w/2, h/2, blx + w/2, bly + h/2, newcap);
//			q2 = new CellTree(w/2, h/2, blx, bly + h/2, newcap);
//			q3 = new CellTree(w/2, h/2, blx, bly, newcap);
//			q4 = new CellTree(w/2, h/2, blx + h/2, bly, newcap);
//
//			split = true;
//
//		}
//		/**
//	 	 * Find objects adjacent or close to o that o might collide with
//	 	 */
//		public ArrayList<GameObject> col(GameObject o){
//          List to store them
//			ArrayList<GameObject> nb = new ArrayList<>();
//			float x = o.getX();
//			float y = o.getY();
//			float rad = o.radius;
//          If the object is out of bounds, then return an empty list
//			if(x - rad > blx + w || y - rad > bly + h || x + rad < blx || y + rad < bly){
//				return nb;
//			}
//
//          //Check what objects are in this node that are close enough to collide with o
//          //and add them to the list
//			for(GameObject t : objects){
//				float tx = t.getX();
//				float ty = t.getY();
//				float trad = t.radius;
//
//				if((rad + trad) * (rad + trad) > (x - tx) * (x - tx) + (y- ty) * (y-ty)){
//					nb.add(t);
//				}
//
//			}
//	        //If this node was split into its child nodes, add everything from each of the quadrants
//			if(split){
//				nb.addAll(q1.col(o));
//				nb.addAll(q2.col(o));
//				nb.addAll(q3.col(o));
//				nb.addAll(q4.col(o));
//			}
//
//			return nb;
//		}
//	}
	//#endregion

	//#region Collision Handlers (DO NOT MODIFY FOR PART 1)
	
	/**
	 * Check if a GameObject is out of bounds and take action.
	 *
	 * Obviously an object off-screen is out of bounds.  In the case of shells, the 
	 * box and bump are also out of bounds.
	 *
	 * @param o      Object to check 
	 * @param offset Offset of the box and bump 
	 */
	private void processBounds(GameObject o, int offset) {
		// Dispatch the appropriate helper for each type
		switch (o.getType()) {
		case NOTE:
			// Only shells care about the offset
			handleBounds((Note)o, offset);
			break;
		case STAR:
			handleBounds((Star)o);
			break;
		case BULLET:
			handleBounds((Bullet)o);
			break;
		case SHIP:
			handleBounds((Ship)o);
			break;
		default:
			break;
		}
	}

	/**
	 * Check a shell for being out-of-bounds.
	 *
	 * Obviously an shell off-screen is out of bounds.  In addition, shells cannot
	 * penetrate the box and bump.
	 *
	 * @param sh     Note to check
	 * @param offset Offset of the box and bump 
	 */
	private void handleBounds(Note sh, int offset) {
		// Hit the rectangular step 
		// (done three times to account for the fact that it could be on the right 
		// side of the screen but also appearing on the left due to scrolling, or 
		//  on the left side but also appearing on the right)
//		hitBox(sh, offset + getBoxX() - getWidth());
//		hitBox(sh, offset + getBoxX());
//		hitBox(sh, offset + getBoxX() + getWidth());
//
//		// Hit the circular bump
//		hitBump(sh, offset + getBumpX() - getWidth());
//		hitBump(sh, offset + getBumpX());
//		hitBump(sh, offset + getBumpX() + getWidth());
		
		// Check if off right side
//		if (sh.getX() > getWidth() - sh.getRadius()) {
//			// Set within bounds on right and swap velocity
//			sh.setX(2 * (getWidth() - sh.getRadius()) - sh.getX());
//			sh.setVX(-sh.getVX());
//		}
//		// Check if off left side
//		else if (sh.getX() < sh.getRadius()) {
//			// Set within bounds on left and swap velocity
//			sh.setX(2 * sh.getRadius() - sh.getX());
//			sh.setVX(-sh.getVX());
//		}
		
		// Check for in bounds on bottom
		if (sh.getY()-sh.getRadius() < -30) {
			// Set within bounds on bottom and swap velocity
			sh.hitStatus = sh.nt == Note.NType.SWITCH ? 0 : -1;
			sh.setDestroyed(true);


			// Constrict velocity
			//sh.setVY((float)Math.max(sh.getMinVY(), sh.getVY() * sh.getFriction()));
		}

	}

	/**
	 * Check a star for being out-of-bounds (currently does nothing).
	 *
	 * @param st Star to check 
	 */
	private void handleBounds(Star st) {
		// DO NOTHING (You may change for Part 2)
	}

	/**
	 * Check a bullet for being out-of-bounds.
	 *
	 * @param bu Bullet to check 
	 */
	private void handleBounds(Bullet bu) {
		// Destroy a bullet once off screen.
		if (bu.getY() <= 0) {
			bu.setDestroyed(true);
		}
	}

	/**
	 * Check a bullet for being out-of-bounds.
	 *
	 * @param sh Ship to check 
	 */
	private void handleBounds(Ship sh) {
		// Do not let the ship go off screen.
		if (sh.getX() <= sh.getRadius()) {
			sh.setX(sh.getRadius());
		} else if (sh.getX() >= getWidth() - sh.getRadius()) {
			sh.setX(getWidth() - sh.getRadius());
		}
	}

	/**
	 * Detect collision with rectangle step in the terrain.
	 *
	 * @param o Object to check 
	 * @param x Offset of the box 
	 */
	private void hitBox(GameObject o, float x) {
		if (Math.abs(o.getX() - x) < getBoxRadius() && o.getY() < getBoxHeight()) {
			if (o.getX()+o.getRadius() > x+getBoxRadius()) {
				o.setX(x+getBoxRadius()+o.getRadius());
				o.setVX(-o.getVX());
			} else if (o.getX()-o.getRadius() < x-getBoxRadius()) {
				o.setX(x-getBoxRadius()-o.getRadius());
				o.setVX(-o.getVX());
			} else {
				o.setVY(-o.getVY() * BOX_COEFF_REST);
				o.setY(getBoxHeight()+o.getRadius());
			}
		}
	}

	/**
	 * Detect collision with semicircular bump in the terrain.
	 *
	 * @param o Object to check 
	 * @param x Offset of the bump 
	 */
	public void hitBump(GameObject o, float x) {
		// Make sure to not just change the velocity but also move the 
		// object so that it no longer penetrates the terrain.
		float dx = o.getX() - x;
		float dy = o.getY() - getFloorLedge();
		float dist = (float)Math.sqrt(dx * dx + dy * dy);
		if (dist < 0.1f * width) {
			float norm_x = dx / dist;
			float norm_y = Math.abs(dy / dist);
			float tmp = (o.getVX() * norm_x + o.getVY() * norm_y)*BUMP_COEFF_REST;
			o.getVelocity().sub(norm_x * tmp, norm_y * tmp);
			o.setY(getFloorLedge() + norm_y * getBumpRadius());
		}
	}
	
	/**
	 * Detect and resolve collisions between two game objects
	 *
	 * @param o1 First object 
	 * @param o2 Second object 
	 */
	private void processCollision(GameObject o1, GameObject o2) {
		// Dispatch the appropriate helper for each type
		switch (o1.getType()) {
		case NOTE:
			switch (o2.getType()) {
			case NOTE:
				handleCollision((Note)o1, (Note)o2);
				break;
			case STAR:
				handleCollision((Note)o1, (Star)o2);
				break;
			case BULLET:
				handleCollision((Note)o1, (Bullet)o2);
				break;
			case SHIP:
				handleCollision((Note)o1, (Ship)o2);
				break;
			default:
				break;
			}
			break;
		case STAR:
			switch (o2.getType()) {
			case NOTE:
				// Reuse shell helper
				handleCollision((Note)o2, (Star)o1);
				break;
			case STAR:
				handleCollision((Star)o1, (Star)o2);
				break;
			case BULLET:
				handleCollision((Star)o1, (Bullet)o2);
				break;
			case SHIP:
				handleCollision((Star)o1, (Ship)o2);
				break;
			default:
				break;
			}
			break;
		case BULLET:
			switch (o2.getType()) {
			case NOTE:
				// Reuse shell helper
				handleCollision((Note)o2, (Bullet)o1);
				break;
			case STAR:
				// Reuse star helper
				handleCollision((Star)o2, (Bullet)o1);
				break;
			case BULLET:
				handleCollision((Bullet)o1, (Bullet)o2);
				break;
			case SHIP:
				handleCollision((Bullet)o1, (Ship)o2);
				break;
			default:
				break;
			}
			break;
		case SHIP:
			switch (o2.getType()) {
			case NOTE:
				// Reuse shell helper
				handleCollision((Note)o2, (Ship)o1);
				break;
			case STAR:
				// Reuse star helper
				handleCollision((Star)o2, (Ship)o1);
				break;
			case BULLET:
				// Reuse bullet helper
				handleCollision((Bullet)o2, (Ship)o1);
				break;
			case SHIP:
				handleCollision((Ship)o1, (Ship)o2);
				break;
			default:
				break;
			}
			break;	
		default:
			break;
		}
	}

	/**
	 * Collide a shell with a shell.
	 *
	 * @param s1 First shell 
	 * @param s2 Second shell 
	 */
	private void handleCollision(Note s1, Note s2) {
		if (s1.isDestroyed() || s2.isDestroyed()) {
			return;
		}



		// Find the axis of "collision"
		temp1.set(s1.getPosition()).sub(s2.getPosition());
		float dist = temp1.len();

		// Too far away
		if (dist > s1.getRadius() + s2.getRadius()) {
			return;
		}

		// Push the shells out so that they do not collide
		float distToPush = 0.01f + (s1.getRadius() + s2.getRadius() - dist) / 2;
		temp1.nor();
		temp1.scl(distToPush);
		s1.getPosition().add(temp1);
		s2.getPosition().sub(temp1);

		// Compute the new velocities
		temp1.set(s2.getPosition()).sub(s1.getPosition()).nor(); // Unit vector for w1
		temp2.set(s1.getPosition()).sub(s2.getPosition()).nor(); // Unit vector for w2
		
		temp1.scl(temp1.dot(s1.getVelocity())); // Scaled to w1
		temp2.scl(temp2.dot(s2.getVelocity())); // Scaled to w2

		// You can remove this to add friction.  We find friction has nasty feedback.
		//temp1.scl(s1.getFriction());
		//temp2.scl(s2.getFriction());
		
		// Apply to the objects
		s1.getVelocity().sub(temp1).add(temp2);
		s2.getVelocity().sub(temp2).add(temp1);
	}

	/**
	 * Collide a shell with a star.
	 *
	 * @param se The shell 
	 * @param st The star 
	 */
	private void handleCollision(Note se, Star st) {
		if (se.isDestroyed() || st.isDestroyed()) {
			return;
		}

		temp1.set(se.getPosition()).sub(st.getPosition());
		float dist = temp1.len();

		// Too far away
		if (dist > se.getRadius() + st.getRadius()) {
			return;
		}

		// Knock back shell
		temp1.nor();
		float dot = temp1.dot(se.getVelocity());
		temp1.scl(dot);
		se.getVelocity().sub(temp1.scl(BUMP_COEFF_REST));

		// Destroy objects
		se.setDestroyed(true);
		st.setDestroyed(true);
	}

	/**
	 * Collide a shell with a bullet.
	 *
	 * @param se The shell 
	 * @param bu The bullet 
	 */
	private void handleCollision(Note se, Bullet bu) {
		if (se.isDestroyed() || bu.isDestroyed()) {
			return;
		}

		temp1.set(se.getPosition()).sub(bu.getPosition());
		float dist = temp1.len();

		// Too far away
		if (dist > se.getRadius() + bu.getRadius()) {
			return;
		}

		// Knock back shell
		temp1.nor();
		float dot = temp1.dot(se.getVelocity());
		temp1.scl(dot);
		se.getVelocity().sub(temp1.scl(BUMP_COEFF_REST));

		// Destroy objects
		se.setDestroyed(true);
		bu.setDestroyed(true);

		if(bu.s != null && se.destroyed){
			bu.s.addpoints();
		}
	}

	/**
	 * Collide a shell with a ship.
	 *
	 * @param se The shell 
	 * @param sh The ship 
	 */
	private void handleCollision(Note se, Ship sh) {
		if (se.isDestroyed() || sh.isDestroyed()) {
			return;
		}

		// Kill the ship if it  is  not invincible

		temp1.set(se.getPosition()).sub(sh.getPosition());
		float dist = temp1.len();

		// Too far away
		if (dist > se.getRadius() + sh.getRadius()) {
			return;
		}

		// Destroy objects
		se.setDestroyed(true);
		sh.setDestroyed(!sh.isInvincible());
	}

	/**
	 * Collide a star with a star.
	 *
	 * @param s1 First star 
	 * @param s2 Second star 
	 */
	private void handleCollision(Star s1, Star s2) {
		// Nothing happens!
	}

	/**
	 * Collide a star with a bullet.
	 *
	 * @param st The star 
	 * @param bu The bullet 
	 */
	private void handleCollision(Star st, Bullet bu) {
		// Nothing happens!
	}

	/**
	 * Collide a star with a ship.
	 *
	 * @param st The star 
	 * @param sh The ship 
	 */
	private void handleCollision(Star st, Ship sh) {
		// Nothing happens!
	}

	/**
	 * Collide a bullet with a bullet.
	 *
	 * @param b1 First bullet 
	 * @param b2 Second bullet 
	 */
	private void handleCollision(Bullet b1, Bullet b2) {
		// Nothing happens!
	}

	/**
	 * Collide a bullet with a ship.
	 *
	 * @param bu The bullet 
	 * @param sh The ship 
	 */
	private void handleCollision(Bullet bu, Ship sh) {
		// Nothing happens!
	}

	/**
	 * Collide a ship with a ship (only useful if you add a 2nd ship)
	 *
	 * @param s1 First ship 
	 * @param s2 Second ship 
	 */
	private void handleCollision(Ship s1, Ship s2) {
		// Prevent them from moving into each other
	}
	
	//#endregion
}