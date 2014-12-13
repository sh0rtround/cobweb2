package org.cobweb.cobweb2.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cobweb.cobweb2.broadcast.BroadcastPacket;
import org.cobweb.cobweb2.core.params.ComplexAgentParams;
import org.cobweb.cobweb2.interconnect.AgentMutator;
import org.cobweb.cobweb2.interconnect.AgentSimilarityCalculator;
import org.cobweb.cobweb2.interconnect.ContactMutator;
import org.cobweb.cobweb2.interconnect.SpawnMutator;
import org.cobweb.cobweb2.interconnect.StepMutator;
import org.cobweb.cobweb2.production.ProductionParams;
import org.cobweb.cobweb2.waste.Waste;

/**
 * TODO better comments
 *
 * <p>During each tick of a simulation, each ComplexAgent instance will
 * be used to call the tickNotification method.  This is done in the
 * TickScheduler.doTick private method.
 *
 * @see org.cobweb.cobweb2.core.Agent
 * @see java.io.Serializable
 *
 */
public class ComplexAgent extends org.cobweb.cobweb2.core.Agent implements Updatable, Serializable {

	/**
	 * This class provides the information of what an agent sees.
	 *
	 */
	public static class SeeInfo {
		private int dist;

		private int type;

		/**
		 * Contains the information of what the agent sees.
		 *
		 * @param d Distance to t.
		 * @param t Type of object seen.
		 */
		public SeeInfo(int d, int t) {
			dist = d;
			type = t;
		}

		/**
		 * @return How far away the object is.
		 */
		public int getDist() {
			return dist;
		}

		/**
		 * @return What the agent sees (rock, food, etc.)
		 */
		public int getType() {
			return type;
		}
	}

	private ProductionParams prodParams;

	/**
	 *
	 */
	private static final long serialVersionUID = -5310096345506441368L;

	/** Default mutable parameters of each agent type. */

	@Deprecated //FIXME static!
	private static ComplexAgentParams defaultParams[];

	@Deprecated //FIXME static!
	private static ProductionParams defaultProdParams[];

	@Deprecated //FIXME static!
	protected static AgentSimilarityCalculator simCalc;

	@Deprecated //FIXME static!
	public static Collection<String> logDataAgent(int i) {
		List<String> blah = new LinkedList<String>();
		for (SpawnMutator mut : spawnMutators) {
			for (String s : mut.logDataAgent(i))
				blah.add(s);
		}
		return blah;
	}

	@Deprecated //FIXME static!
	public static Iterable<String> logDataTotal() {
		List<String> blah = new LinkedList<String>();
		for (SpawnMutator mut : spawnMutators) {
			for (String s : mut.logDataTotal())
				blah.add(s);
		}
		return blah;
	}

	@Deprecated //FIXME static!
	public static Collection<String> logHederAgent() {
		List<String> blah = new LinkedList<String>();
		for (SpawnMutator mut : spawnMutators) {
			for (String s : mut.logHeadersAgent())
				blah.add(s);
		}
		return blah;
	}

	@Deprecated //FIXME static!
	public static Iterable<String> logHederTotal() {
		List<String> blah = new LinkedList<String>();
		for (SpawnMutator mut : spawnMutators) {
			for (String s : mut.logHeaderTotal())
				blah.add(s);
		}
		return blah;
	}

	/** Sets the default mutable parameters of each agent type. */
	@Deprecated //FIXME static!
	public static void setDefaultMutableParams(ComplexAgentParams[] params, ProductionParams[] pParams) {
		defaultParams = params.clone();
		for (int i = 0; i < params.length; i++) {
			defaultParams[i] = (ComplexAgentParams) params[i].clone();
		}

		if (pParams != null) {
			defaultProdParams = pParams.clone();
			for (int i = 0; i < pParams.length; i++) {
				defaultProdParams[i] = (ProductionParams) pParams[i].clone();
			}
		}
	}

	@Deprecated //FIXME static!
	public static void setSimularityCalc(AgentSimilarityCalculator calc) {
		simCalc = calc;
	}

	/**
	 * The agent's type.
	 */
	protected int agentType = 0;

	public ComplexAgentParams params;

	/**
	 * Energy gauge
	 */
	protected int energy;
	/** Prisoner's Dilemma */
	public boolean pdCheater; // The agent's action; 1 == cheater, else cooperator
	private boolean lastPDcheated; // Remember the opponent's move in the last game

	private int commInbox;

	private int commOutbox;
	// memory size is the maximum capacity of the number of cheaters an agent
	// can remember
	protected long photo_memory[];
	private int photo_num = 0;
	protected boolean want2meet = false;

	/* Waste variables */
	private int wasteCounterGain;
	private int wasteCounterLoss;

	private int memoryBuffer;


	protected ComplexAgent breedPartner;

	private boolean asexFlag;

	protected ComplexAgentStatistics stats;

	// pregnancyPeriod is set value while pregPeriod constantly changes
	protected int pregPeriod;

	protected boolean pregnant = false;

	public static final int LOOK_DISTANCE = 4;

	/** The current tick we are in (or the last tick this agent was notified */
	protected long currTick = 0;

	@Deprecated //FIXME static!
	protected static Set<ContactMutator> contactMutators = new LinkedHashSet<ContactMutator>();

	@Deprecated //FIXME static!
	protected static Set<StepMutator> stepMutators = new LinkedHashSet<StepMutator>();

	private static final org.cobweb.cobweb2.core.Direction[] dirList = { org.cobweb.cobweb2.core.Environment.DIRECTION_NORTH,
		org.cobweb.cobweb2.core.Environment.DIRECTION_SOUTH, org.cobweb.cobweb2.core.Environment.DIRECTION_WEST, org.cobweb.cobweb2.core.Environment.DIRECTION_EAST,
		org.cobweb.cobweb2.core.Environment.DIRECTION_NORTHEAST, org.cobweb.cobweb2.core.Environment.DIRECTION_SOUTHEAST,
		org.cobweb.cobweb2.core.Environment.DIRECTION_NORTHWEST, org.cobweb.cobweb2.core.Environment.DIRECTION_SOUTHWEST };

	@Deprecated //FIXME static!
	public static void addMutator(AgentMutator mutator) {
		if (mutator instanceof SpawnMutator)
			spawnMutators.add((SpawnMutator) mutator);

		if (mutator instanceof ContactMutator)
			contactMutators.add((ContactMutator) mutator);

		if (mutator instanceof StepMutator)
			stepMutators.add((StepMutator) mutator);
	}

	@Deprecated //FIXME static!
	public static void clearMutators() {
		spawnMutators.clear();
		contactMutators.clear();
		stepMutators.clear();
	}

	@Deprecated //FIXME static!
	private static Set<SpawnMutator> spawnMutators = new LinkedHashSet<SpawnMutator>();

	public transient ComplexEnvironment environment;

	public ComplexAgent(SimulationInternals sim) {
		super(sim);
	}

	/**
	 * Constructor with two parents
	 *
	 * @param pos spawn position
	 * @param parent1 first parent
	 * @param parent2 second parent
	 */
	public void init(ComplexEnvironment env, LocationDirection pos, ComplexAgent parent1, ComplexAgent parent2) {
		environment = env;
		init(env.controllerFactory.createFromParents(parent1.getController(), parent2.getController(),
				parent1.params.mutationRate));

		copyConstants(parent1);

		// child's strategy is determined by its parents, it has a
		// 50% chance to get either parent's strategy
		if (simulation.getRandom().nextBoolean()) {
			params.pdCoopProb = parent2.params.pdCoopProb;
			params.pdTitForTat = parent2.params.pdTitForTat;
			params.pdSimilarityNeutral = parent2.params.pdSimilarityNeutral;
			params.pdSimilaritySlope = parent2.params.pdSimilaritySlope;
		} // else keep parent 1's PD config

		stats = environment.addAgentInfo(agentType, parent1.stats, parent2.stats);

		move(pos);
		InitFacing();

		environment.simulation.addAgent(this);

		for (SpawnMutator mutator : spawnMutators)
			mutator.onSpawn(this, parent1, parent2);

	}


	/**
	 * Constructor with a parent; standard asexual copy
	 *
	 * @param pos spawn position
	 * @param parent parent
	 */
	protected void init(ComplexEnvironment env, LocationDirection pos, ComplexAgent parent) {
		environment = (env);
		init(env.controllerFactory.createFromParent(parent.getController(), parent.params.mutationRate));

		copyConstants(parent);
		stats = environment.addAgentInfo(agentType, parent.stats);

		move(pos);
		InitFacing();

		environment.simulation.addAgent(this);

		for (SpawnMutator mutator : spawnMutators)
			mutator.onSpawn(this, parent);
	}

	/**
	 * Constructor with no parent agent; creates an agent using "immaculate conception" technique
	 *
	 * @param agentType agent type
	 * @param pos spawn position
	 * @param agentData agent parameters
	 */
	public void init(ComplexEnvironment env, int agentType, LocationDirection pos, ComplexAgentParams agentData, ProductionParams prodData) {
		environment = (env);
		init(env.controllerFactory.createNew(agentData.memoryBits, agentData.communicationBits, agentType));
		setConstants(agentData, prodData);

		params = agentData;
		stats = environment.addAgentInfo(agentType);
		this.agentType = agentType;

		move(pos);
		InitFacing();

		environment.simulation.addAgent(this);

		for (SpawnMutator mutator : spawnMutators)
			mutator.onSpawn(this);
	}

	private void afterTurnAction() {
		energy -= energyPenalty();
		if (energy <= 0)
			die();
		if (!pregnant)
			tryAsexBreed();
		if (pregnant) {
			pregPeriod--;
		}
	}

	void broadcastCheating(int cheaterID) { // []SK
		String message = Long.toString(cheaterID);
		BroadcastPacket msg = new BroadcastPacket(BroadcastPacket.CHEATER, id, message, energy
				, params.broadcastEnergyBased, params.broadcastFixedRange, getPosition());
		environment.commManager.addPacketToList(msg);
		// new CommPacket sent
		energy -= params.broadcastEnergyCost; // Deduct broadcasting cost from energy
	}

	/**
	 * Creates a new communication packet.  The energy to broadcast is
	 * deducted here.
	 *
	 * @param loc The location of food.
	 */
	protected void broadcastFood(org.cobweb.cobweb2.core.Location loc) { // []SK
		String message = loc.toString();
		BroadcastPacket msg = new BroadcastPacket(BroadcastPacket.FOOD, id, message, energy
				, params.broadcastEnergyBased, params.broadcastFixedRange, getPosition());
		environment.commManager.addPacketToList(msg);
		// new CommPacket sent
		energy -= params.broadcastEnergyCost; // Deduct broadcasting cost from energy
	}

	/**
	 * @return True if agent has enough energy to broadcast
	 */
	protected boolean canBroadcast() {
		return energy > params.broadcastEnergyMin;
	}

	/**
	 * @param destPos The location of the agents next position.
	 * @return True if agent can eat this type of food.
	 */
	public boolean canEat(org.cobweb.cobweb2.core.Location destPos) {
		return params.foodweb.canEatFood[environment.getFoodType(destPos)];
	}

	/**
	 * @param adjacentAgent The agent attempting to eat.
	 * @return True if the agent can eat this type of agent.
	 */
	protected boolean canEat(ComplexAgent adjacentAgent) {
		boolean caneat = false;
		caneat = params.foodweb.canEatAgent[adjacentAgent.getAgentType()];
		if (this.energy > params.breedEnergy)
			caneat = false;

		return caneat;
	}

	/**
	 * @param destPos The location of the agents next position.
	 * @return True if location exists and is not occupied by anything
	 */
	protected boolean canStep(Location destPos) {
		// The position must be valid...
		if (destPos == null)
			return false;
		// and the destination must be clear of stones
		if (environment.testFlag(destPos, ComplexEnvironment.FLAG_STONE))
			return false;
		// and clear of wastes
		if (environment.testFlag(destPos, ComplexEnvironment.FLAG_DROP))
			return environment.getDrop(destPos).canStep();
		// as well as other agents...
		if (environment.getAgent(destPos) != null)
			return false;
		return true;
	}

	boolean checkCredibility(long agentId) {
		// check if dispatcherId is in list
		// if (agentId != null) {
		for (int i = 0; i < params.pdMemory; i++) {
			if (photo_memory[i] == agentId) {
				return false;
			}
		}
		// }
		return true;
	}


	BroadcastPacket checkforBroadcasts() {
		return environment.commManager.findPacket(getPosition());
	}

	//@Override
	//	public Object clone() {
	//		ComplexAgent cp = new ComplexAgent(getAgentType(), pdCheater, params, getPosition().direction);
	//		//cp.hibernate();
	//		return cp;
	//	}

	protected void communicate(ComplexAgent target) {
		target.setCommInbox(getCommOutbox());
	}

	public void copyConstants(ComplexAgent p) {
		setConstants((ComplexAgentParams) defaultParams[p.getAgentType()].clone(),
				(ProductionParams) defaultProdParams[p.getAgentType()].clone());
		pdCheater = p.pdCheater;
	}

	@Override
	public void die() {
		super.die();

		environment.setAgent(position, null);

		for (SpawnMutator mutator : spawnMutators) {
			mutator.onDeath(this);
		}

		stats.setDeath(environment.simulation.getTime());
	}

	/**
	 * This method allows the agent to see what is in front of it.
	 *
	 * @return What the agent sees and at what distance.
	 */
	public SeeInfo distanceLook() {
		LocationDirection destPos = environment.getAdjacent(getPosition());

		for (int dist = 1; dist <= LOOK_DISTANCE; ++dist) {

			// We are looking at the wall
			if (destPos == null)
				return new SeeInfo(dist, ComplexEnvironment.FLAG_STONE);

			// Check for stone...
			if (environment.testFlag(destPos, ComplexEnvironment.FLAG_STONE))
				return new SeeInfo(dist, ComplexEnvironment.FLAG_STONE);

			// If there's another agent there, then return that it's a stone...
			if (environment.getAgent(destPos) != null && environment.getAgent(destPos) != this)
				return new SeeInfo(dist, ComplexEnvironment.FLAG_AGENT);

			// If there's food there, return the food...
			if (environment.testFlag(destPos, ComplexEnvironment.FLAG_FOOD))
				return new SeeInfo(dist, ComplexEnvironment.FLAG_FOOD);

			if (environment.testFlag(destPos, ComplexEnvironment.FLAG_DROP))
				return new SeeInfo(dist, ComplexEnvironment.FLAG_DROP);

			destPos = environment.getAdjacent(destPos);
		}
		return new SeeInfo(LOOK_DISTANCE, 0);
	}

	/**
	 * The agent eats the food (food flag is set to false), and
	 * gains energy and waste according to the food type.
	 *
	 * @param destPos Location of food.
	 */
	public void eat(org.cobweb.cobweb2.core.Location destPos) {
		// TODO: CHECK if setting flag before determining type is ok
		// Eat first before we can produce waste, of course.
		environment.setFlag(destPos, ComplexEnvironment.FLAG_FOOD, false);
		// Gain Energy according to the food type.
		if (environment.getFoodType(destPos) == agentType) {
			energy += params.foodEnergy;
			wasteCounterGain -= params.foodEnergy;
			stats.addFoodEnergy(params.foodEnergy);
		} else {
			energy += params.otherFoodEnergy;
			wasteCounterGain -= params.otherFoodEnergy;
			stats.addOthers(params.otherFoodEnergy);
		}
	}

	/**
	 * The agent eats the adjacent agent by killing it and gaining
	 * energy from it.
	 *
	 * @param adjacentAgent The agent being eaten.
	 */
	protected void eat(ComplexAgent adjacentAgent) {
		int gain = (int) (adjacentAgent.energy * params.agentFoodEnergy);
		energy += gain;
		wasteCounterGain -= gain;
		stats.addCannibalism(gain);
		adjacentAgent.die();
	}

	public double energyPenalty() {
		if (!params.agingMode)
			return 0.0;
		double tempAge = getAge();
		int penaltyValue = Math.min(Math.max(0, energy), (int)(params.agingRate
				* (Math.tan(((tempAge / params.agingLimit) * 89.99) * Math.PI / 180))));

		return penaltyValue;
	}

	protected Agent getAdjacentAgent() {
		org.cobweb.cobweb2.core.Location destPos = environment.getAdjacent(getPosition(), getPosition().direction);
		if (destPos == null) {
			return null;
		}
		return environment.getAgent(destPos);
	}

	public long getAge() {
		return currTick - stats.birthTick;
	}

	public boolean getAgentPDActionCheat() {
		return pdCheater;
	}

	public int getAgentType() {
		return params.type;
	}


	public int getCommInbox() {
		return commInbox;
	}

	public int getCommOutbox() {
		return commOutbox;
	}

	/**
	 * return Agent's energy
	 */
	@Override
	public int getEnergy() {
		return energy;
	}

	public ComplexAgentStatistics getInfo() {
		return stats;
	}

	/**
	 * North = 0
	 * <br>East = 1
	 * <br>South = 2
	 * <br>West = 3
	 *
	 * @return A number representation of the direction the agent is getPosition().direction.
	 */
	public int getIntFacing() {
		if (getPosition().direction.equals(org.cobweb.cobweb2.core.Environment.DIRECTION_NORTH))
			return 0;
		if (getPosition().direction.equals(org.cobweb.cobweb2.core.Environment.DIRECTION_EAST))
			return 1;
		if (getPosition().direction.equals(org.cobweb.cobweb2.core.Environment.DIRECTION_SOUTH))
			return 2;
		if (getPosition().direction.equals(org.cobweb.cobweb2.core.Environment.DIRECTION_WEST))
			return 3;
		return 0;
	}

	public Direction getFacing() {
		return getPosition().direction;
	}

	public int getMemoryBuffer() {
		return memoryBuffer;
	}

	/**
	 * Provide a random direction for the agent to face.
	 */
	private void InitFacing() {
		int f = simulation.getRandom().nextInt(4);
		if (f == 0)
			position = new LocationDirection(position, Environment.DIRECTION_NORTH);
		else if (f == 1)
			position = new LocationDirection(position, Environment.DIRECTION_SOUTH);
		else if (f == 2)
			position = new LocationDirection(position, Environment.DIRECTION_EAST);
		else
			position = new LocationDirection(position, Environment.DIRECTION_WEST);
	}

	public boolean isAsexFlag() {
		return asexFlag;
	}

	/**
	 * The agent will remember the last variable number of agents that
	 * cheated it.  How many cheaters it remembers is determined by its
	 * PD memory size.
	 *
	 * @param othersID In a game of PD, the opposing agents ID
	 */
	protected void iveBeenCheated(int othersID) {

		if (params.pdMemory > 0) {
			photo_memory[photo_num++] = othersID;

			if (photo_num >= params.pdMemory) {
				photo_num = 0;
			}
		}

		broadcastCheating(othersID);
	}

	public void move(LocationDirection newPos) {
		environment.setAgent(newPos, this);
		if (position != null)
			environment.setAgent(position, null);
		position = newPos;

		stats.addPathStep(newPos);
	}

	/**
	 * This method initializes the agents actions in an iterated prisoner's
	 * dilemma game.  The agent can use the following strategies described
	 * by the agentPDStrategy integer:
	 *
	 * <p>0. Default
	 *
	 * <p>The agents decision to defect or cooperate is chosen randomly.
	 * The probability of choosing either is determined by the agents
	 * pdCoopProb parameter.
	 *
	 * <p>1. Tit for Tat
	 *
	 * <p>The agent will initially begin with a cooperate, but will then choose
	 * whatever the opposing agent chose last.  For example, the agent begins
	 * with a cooperate, but if the opposing agent has chosen to defect, then
	 * the agent will choose to defect next round.
	 *
	 */
	public void playPD(ComplexAgent other) {

		double coopProb = params.pdCoopProb / 100.0d;

		float similarity = simCalc.similarity(this, other);

		coopProb += (similarity - params.pdSimilarityNeutral) * params.pdSimilaritySlope;

		if (params.pdTitForTat) { // if true then agent is playing TitForTat
			pdCheater = lastPDcheated;
		} else {
			pdCheater = false; // agent is assumed to cooperate
			float rnd = simulation.getRandom().nextFloat();
			if (rnd > coopProb)
				pdCheater = true; // agent defects depending on
			// probability
		}

		return;
	}

	/**
	 *Prisoner's dilemma is played between the two agents using the strategies
	 *assigned in playPD().  The agent will use its PD memory to remember agents
	 *that cheat it, which will affect whether an agent will want to meet another,
	 *and its credibility.
	 *
	 *<p>How Prisoner's Dilemma is played:
	 *
	 *<p>Prisoner's dilemma is a game between two agents when they come in to
	 *contact with each other.  The game determines how much energy each agent
	 *receives after contact.  Each agent has two options: cooperate or defect.
	 *The agents choice to cooperate or defect is determined by the strategy the
	 *agent is using (see playPD() method).  The agents choices can lead to
	 *one of four outcomes:
	 *
	 *<p> 1. REWARD for mutual cooperation (Both agents cooperate)
	 *
	 *<p> 2. SUCKER's payoff (Opposing agent defects; this agent cooperates)
	 *
	 *<p> 3. TEMPTATION to defect (Opposing agent cooperates; this agent defects)
	 *
	 *<p> 4. PUNISHMENT for mutual defection (Both agents defect)
	 *
	 *<p>The best strategy for both agents is to cooperate.  However, if an agent
	 *chooses to defect when the other cooperates, the defecting agent will have
	 *a greater advantage.  For a true game of PD, the energy scores for each
	 *outcome should follow this rule: TEMPTATION > REWARD > PUNISHMENT > SUCKER
	 *
	 *<p>Here is an example of how much energy an agent could receive:
	 *<br> REWARD     =>     5
	 *<br> SUCKER     =>     2
	 *<br> TEMPTATION =>     8
	 *<br> PUNISHMENT =>     3
	 *
	 * @param adjacentAgent Agent playing PD with
	 * @param othersID ID of the adjacent agent.
	 * @see ComplexAgent#playPD()
	 * @see <a href="http://en.wikipedia.org/wiki/Prisoner's_dilemma">Prisoner's Dilemma</a>
	 */
	@SuppressWarnings("javadoc")
	public void playPDonStep(ComplexAgent adjacentAgent, int othersID) {
		if (!environment.isPDenabled())
			return;

		playPD(adjacentAgent);
		adjacentAgent.playPD(this);

		// Save result for future strategy (tit-for-tat, learning, etc.)
		lastPDcheated = adjacentAgent.pdCheater;
		adjacentAgent.lastPDcheated = pdCheater;

		/*
		 * TODO LOW: The ability for the PD game to contend for the Get the food tiles immediately around each agents
		 */

		if (!pdCheater && !adjacentAgent.pdCheater) {
			/* Both cooperate */
			energy += environment.PD_PAYOFF_REWARD;
			adjacentAgent.energy += environment.PD_PAYOFF_REWARD;
			stats.addPDReward();

		} else if (!pdCheater && adjacentAgent.pdCheater) {
			/* Only other agent cheats */
			energy += environment.PD_PAYOFF_SUCKER;
			adjacentAgent.energy += environment.PD_PAYOFF_TEMPTATION;
			stats.addPDTemptation();

		} else if (pdCheater && !adjacentAgent.pdCheater) {
			/* Only this agent cheats */
			energy += environment.PD_PAYOFF_TEMPTATION;
			adjacentAgent.energy += environment.PD_PAYOFF_SUCKER;
			stats.addPDSucker();

		} else if (pdCheater && adjacentAgent.pdCheater) {
			/* Both cheat */
			energy += environment.PD_PAYOFF_PUNISHMENT;
			adjacentAgent.energy += environment.PD_PAYOFF_PUNISHMENT;
			stats.addPDPunishment();

		}

		if (adjacentAgent.pdCheater)
			iveBeenCheated(othersID);
	}

	protected void receiveBroadcast() {
		BroadcastPacket commPacket = null;

		commPacket = checkforBroadcasts();
		if (commPacket == null)
			return;

		// check if dispatcherId is in list
		// TODO what does this do?
		checkCredibility(commPacket.getDispatcherId());

		int type = commPacket.getType();
		switch (type) {
			case BroadcastPacket.FOOD:
				receiveFoodBroadcast(commPacket);
				break;
			case BroadcastPacket.CHEATER:
				receiveCheatingBroadcast(commPacket);
				break;
			default:
				Logger myLogger = Logger.getLogger("COBWEB2");
				myLogger.log(Level.WARNING, "Unrecognised broadcast type");
		}
	}

	void receiveCheatingBroadcast(BroadcastPacket commPacket) {
		String message = commPacket.getContent();
		long cheaterId = 0;
		cheaterId = Long.parseLong(message);
		photo_memory[photo_num] = cheaterId;
	}

	void receiveFoodBroadcast(BroadcastPacket commPacket) {
		String message = commPacket.getContent();
		String[] xy = message.substring(1, message.length() - 1).split(",");
		int x = Integer.parseInt(xy[0]);
		int y = Integer.parseInt(xy[1]);
		thinkAboutFoodLocation(x, y);

	}

	public void setAsexFlag(boolean asexFlag) {
		this.asexFlag = asexFlag;
	}

	public void setCommInbox(int commInbox) {
		this.commInbox = commInbox;
	}

	public void setCommOutbox(int commOutbox) {
		this.commOutbox = commOutbox;
	}

	/**
	 * Sets the complex agents parameters.
	 *
	 * @param agentData The ComplexAgentParams used for this complex agent.
	 */
	public void setConstants(ComplexAgentParams agentData, ProductionParams prodData) {

		this.params = agentData;
		this.prodParams = prodData;

		this.agentType = agentData.type;

		energy = agentData.initEnergy;
		wasteCounterGain = params.wasteLimitGain;
		setWasteCounterLoss(params.wasteLimitLoss);

		photo_memory = new long[params.pdMemory];

		this.lastPDcheated = false;
		// "KeepOldAgents" need pass this
		// parameter. (as a reasonable side
		// effect, the parameter of a parent
		// would also pass to its child)
		// See ComplexEnvironment.load(cobweb.Scheduler s, Parser p/*
		// java.io.Reader r */) @ if (keepOldAgents[0]) {...

	}

	public void setMemoryBuffer(int memoryBuffer) {
		this.memoryBuffer = memoryBuffer;
	}

	/**
	 * During a step, the agent can encounter four different circumstances:
	 * 1. Nothing is in its way.
	 * 2. Contact with another agent.
	 * 3. Run into waste.
	 * 4. Run into a rock.
	 *
	 * <p> 1. Nothing in its way:
	 *
	 * <p>If the agent can move into the next position, the first thing it will do
	 * is check for food.  If it finds food, then the agent may
	 * broadcast a message containing the location of the food.  The agent may
	 * then eat the food.  If after eating the food the agent was pregnant, a check
	 * will be made to see if the child can be produced now.  If the agent was not
	 * pregnant, then a-sexual breeding will be attempted.
	 *
	 * <p>This method will then iterate through all  mutators used in the simulation
	 * and call onStep for each step mutator.  The agent will then move.  If it
	 * was found that the agent was ready to produce a child, then a new agent
	 * is created.
	 *
	 * <p> 2. Contact with another agent:
	 *
	 * <p> Contact mutators are iterated through and the onContact method is called
	 * for each used within the simulation.  The agent will eat the agent if it can.
	 *
	 * <p> If prisoner's dilemma is being used for this simulation, then a check is
	 * made to see if both agents want to meet each other (True if no bad memories of
	 * adjacent agent).  If the adjacent agent was not eaten and both agents want to
	 * meet each other, then the possibility of breeding will be looked in to.  If
	 * breeding is not possible, then prisoner's dilemma will be played.  If prisoner's
	 * dilemma is not used, then only breeding is checked for.
	 *
	 * <p> An energy penalty is deducted for bumping into another agent.
	 *
	 * <p> 3 and 4. Run into waste/rock:
	 *
	 * <p> Energy penalties are deducted from the agent.
	 *
	 * @see ComplexAgent#playPDonStep(ComplexAgent, int)
	 */
	public void step() {
		org.cobweb.cobweb2.core.Agent adjAgent;
		LocationDirection destPos = environment.getAdjacent(getPosition());

		if (canStep(destPos)) {

			onstepFreeTile(destPos);

		} else if ((adjAgent = getAdjacentAgent()) != null && adjAgent instanceof ComplexAgent
				&& ((ComplexAgent) adjAgent).stats != null) {
			// two agents meet

			ComplexAgent adjacentAgent = (ComplexAgent) adjAgent;


			onstepAgentBump(adjacentAgent);

		} // end of two agents meet
		else {
			// Non-free tile (rock/waste/etc) bump
			energy -= params.stepRockEnergy;
			wasteCounterLoss -= params.stepRockEnergy;
			stats.useRockBumpEnergy(params.stepRockEnergy);
		}
		energy -= energyPenalty();

		if (destPos != null && environment.testFlag(destPos, ComplexEnvironment.FLAG_DROP)) {
			// Bumps into drop
			Drop d = environment.getDrop(destPos);

			if (d.canStep()) {
				d.onStep(this);
			}
			else {
				// can't step, treat as obstacle
				stats.useRockBumpEnergy(params.stepRockEnergy);
			}
		}

		if (energy <= 0)
			die();

		if (energy < params.breedEnergy) {
			pregnant = false;
			breedPartner = null;
		}

		if (pregnant) {
			pregPeriod--;
		}
	}

	protected void onstepFreeTile(LocationDirection destPos) {
		// Check for food...
		LocationDirection breedPos = null;
		if (environment.testFlag(destPos, ComplexEnvironment.FLAG_FOOD)) {
			if (params.broadcastMode && canBroadcast()) {
				broadcastFood(destPos);
			}
			if (canEat(destPos)) {
				eat(destPos);
			}
		}

		if (pregnant && energy >= params.breedEnergy && pregPeriod <= 0) {
			breedPos = new LocationDirection(getPosition(), Environment.DIRECTION_NONE);
		} else if (!pregnant) {
			// TODO: make AI control this choice?
			tryAsexBreed();
		}

		for (StepMutator m : stepMutators)
			m.onStep(this, destPos, getPosition());

		move(destPos);

		if (breedPos != null) {
			energy -= params.initEnergy;
			energy -= energyPenalty();
			wasteCounterLoss -= params.initEnergy;
			stats.useReproductionEnergy(params.initEnergy);
			stats.addDirectChild();

			ComplexAgent child = simulation.newAgent();

			if (breedPartner == null) {
				child.init(environment, breedPos, this);
			} else {
				breedPartner.stats.addDirectChild();
				child.init(environment, breedPos, this, breedPartner);
				stats.addSexPreg();
			}
			breedPartner = null;
			pregnant = false;
		}
		energy -= params.stepEnergy;
		wasteCounterLoss -= params.stepEnergy;
		stats.useStepEnergy(params.stepEnergy);
	}

	protected void onstepAgentBump(ComplexAgent adjacentAgent) {
		for (ContactMutator mut : contactMutators) {
			mut.onContact(this, adjacentAgent);
		}

		if (canEat(adjacentAgent)) {
			eat(adjacentAgent);
		}

		want2meet = true;

		int othersID = adjacentAgent.stats.getAgentNumber();
		// scan the memory array, is the 'other' agents ID is found in the array,
		// then choose not to have a transaction with him.
		for (int i = 0; i < params.pdMemory; i++) {
			if (photo_memory[i] == othersID) {
				want2meet = false;
			}
		}
		// if the agents are of the same type, check if they have enough
		// resources to breed
		if (adjacentAgent.agentType == agentType) {

			double sim = 0.0;
			boolean canBreed = !pregnant && energy >= params.breedEnergy && params.sexualBreedChance != 0.0
					&& simulation.getRandom().nextFloat() < params.sexualBreedChance;

			// Generate genetic similarity number
			sim = simCalc.similarity(this, adjacentAgent);

			if (sim >= params.commSimMin) {
				communicate(adjacentAgent);
			}

			if (canBreed && sim >= params.breedSimMin
					&& (want2meet && adjacentAgent.want2meet)) {
				pregnant = true;
				pregPeriod = params.sexualPregnancyPeriod;
				breedPartner = adjacentAgent;
			}
		}
		// perform the transaction only if non-pregnant and both agents want to meet
		if (!pregnant && want2meet && adjacentAgent.want2meet) {

			playPDonStep(adjacentAgent, othersID);
		}
		energy -= params.stepAgentEnergy;
		setWasteCounterLoss(getWasteCounterLoss() - params.stepAgentEnergy);
		stats.useAgentBumpEnergy(params.stepAgentEnergy);
	}

	private void thinkAboutFoodLocation(int x, int y) {
		Location target = environment.getLocation(x, y);

		double closeness = 1;

		if (!target.equals(getPosition()))
			closeness = 1 / target.distance(this.getPosition());

		int o =(int)Math.round(closeness * ((1 << this.params.communicationBits) - 1));

		setCommInbox(o);
	}

	/**
	 * Controls what happens to the agent on this tick.  If the
	 * agent is still alive, what happens to the agent is determined
	 * by the controller.
	 *
	 * @param tick The time in the simulation
	 */
	@Override
	public void update(long tick) {
		if (!isAlive())
			return;

		//update current tick
		currTick = tick;


		/* Time to die, Agent (mister) Bond */
		if (params.agingMode) {
			if ((getAge()) >= params.agingLimit) {
				die();
				return;
			}
		}

		/* Check if broadcasting is enabled */
		if (params.broadcastMode)
			receiveBroadcast();

		// If updateAgent called die();
		if (!isAlive())
			return;

		controller.controlAgent(this);

		/* Produce waste if able */
		if (params.wasteMode && shouldPoop())
			tryPoop();

		if (prodParams.productionMode) {
			tryProduction();
		}
	}

	/**
	 * If the agent has enough energy to breed, is randomly chosen to breed,
	 * and its asexFlag is true, then the agent will be pregnant and set to
	 * produce a child agent after the agent's asexPregnancyPeriod is up.
	 */
	protected void tryAsexBreed() {
		if (isAsexFlag() && energy >= params.breedEnergy && params.asexualBreedChance != 0.0
				&& simulation.getRandom().nextFloat() < params.asexualBreedChance) {
			pregPeriod = params.asexPregnancyPeriod;
			pregnant = true;
		}
	}

	private boolean shouldPoop() {
		if (wasteCounterGain <= 0 && params.wasteLimitGain > 0) {
			wasteCounterGain += params.wasteLimitGain;
			return true;
		} else if (getWasteCounterLoss() <= 0 && params.wasteLimitLoss > 0) {
			setWasteCounterLoss(getWasteCounterLoss() + params.wasteLimitLoss);
			return true;
		}
		return false;
	}


	/**
	 * Produce waste
	 */
	private void tryPoop() {
		forceDrop(new Waste(currTick, params.wasteInit, params.wasteDecay));
	}

	private void forceDrop(Drop d) {
		boolean added = false;

		// For this method, "adjacent" implies tiles around the agent including
		// tiles that are diagonally adjacent

		org.cobweb.cobweb2.core.Location loc;

		// Place the drop at an available location adjacent to the agent
		for (int i = 0; i < dirList.length; i++) {
			loc = environment.getAdjacent(getPosition(), dirList[i]);
			if (loc != null && environment.getAgent(loc) == null
					&& !environment.testFlag(loc, ComplexEnvironment.FLAG_STONE)
					&& !environment.testFlag(loc, ComplexEnvironment.FLAG_DROP)
					&& !environment.testFlag(loc, ComplexEnvironment.FLAG_FOOD)) {
				environment.setFlag(loc, ComplexEnvironment.FLAG_FOOD, false);
				environment.setFlag(loc, ComplexEnvironment.FLAG_STONE, false);
				environment.setFlag(loc, ComplexEnvironment.FLAG_DROP, true);
				environment.setDrop(loc, d);
				break;
			}
		}

		/*
		 * Crowded! IF there is no empty tile in which to drop the waste, we can replace a food tile with a waste
		 * tile... / This function is assumed to add a waste tile! That is, this function assumes an existence of at
		 * least one food tile that it will be able to replace with a waste tile. Nothing happens otherwise.
		 */
		if (!added) {
			for (int i = 0; i < dirList.length; i++) {
				loc = environment.getAdjacent(getPosition(), dirList[i]);
				if (loc != null
						&& environment.getAgent(loc) == null
						&& environment.testFlag(loc, ComplexEnvironment.FLAG_FOOD)) {
					environment.setFlag(loc, ComplexEnvironment.FLAG_FOOD, false);
					environment.setFlag(loc, ComplexEnvironment.FLAG_DROP, true);
					environment.setDrop(loc, d);
					break;
				}
			}
		}
	}
	private boolean roll(float chance) {
		return chance > simulation.getRandom().nextFloat();
	}

	boolean shouldProduce() {
		if (prodParams == null || !roll(prodParams.initProdChance)) {
			return false;
		}

		float locationValue = environment.prodMapper.getValueAtLocation(position);

		if (locationValue > prodParams.highDemandCutoff) {
			return false;
		}

		// ADDITIONS:
		// Learning agents should adapt to products

		if (locationValue <= prodParams.lowDemandThreshold) {
			// In an area of low demand
			return roll(prodParams.lowDemandProdChance);
		} else if (locationValue <= prodParams.sweetDemandThreshold) {
			/*
			 * The sweet spot is an inverted parabola, the vertex is 100% probability in the middle of the sweet spot
			 * (between lowDemandThreshold and sweetDemandThreshold)
			 * the tips are sweetDemandStartChance probability at the thresholds.
			 *
			 */

			// parabola shape
			float peak = (prodParams.lowDemandThreshold + prodParams.sweetDemandThreshold) * 0.5f;
			float width = prodParams.sweetDemandThreshold - prodParams.lowDemandThreshold;
			// position along standard parabola
			float x = (locationValue - peak) / (width / 2);
			// parabola value
			float y = x * x  * (1-prodParams.sweetDemandStartChance);

			float chance = prodParams.sweetDemandStartChance + (1 - y);

			// Sweet spot; perfect balance of competition and attraction here;
			// likelihood of producing products here
			// is modelled by a parabola
			return roll(chance);
		}

		// locationValue > 10f; Very high competition in this area!
		// The higher the value the lower the production chances are.

		// Let: d = prodParams.sweetDemandThreshold
		// e = prodParams.highDemandCutoff
		// f = prodParams.highDemandProdChance
		//
		// p1 = (d, f);
		// p2 = (e, 0);
		//
		// rise = f - 0 = f;
		// run = d - e
		//
		// m = f / (d - e)
		//
		// y = mx + b
		//
		// b = y - mx
		// b = 0 - me
		// b = -(f / (d -e))e
		//
		// y = ((f - e) / d)x + e

		float d = prodParams.sweetDemandThreshold;
		float e = prodParams.highDemandCutoff;
		float f = prodParams.highDemandProdChance;

		float rise = f;
		float run = d - e;

		float m = rise / run;

		float b = -1 * m * e;

		// y = mx + b
		float y = (m * locationValue) + b;

		// p1 = (sweetDemandThreshold, prodParams.highDemandProdChance)
		// p2 = (
		// minChance

		return roll(y);
	}

	private void tryProduction() {
		if (shouldProduce()) {
			// TODO: find a more clean way to create and assign product
			// Healthy agents produce high-value products, and vice-versa
			environment.prodMapper.createProduct((float) energy / (float) params.initEnergy, this);

			if (environment.testFlag(position, ComplexEnvironment.FLAG_FOOD)) {
				environment.setFlag(position, ComplexEnvironment.FLAG_FOOD, false);
			}

			environment.setFlag(position, ComplexEnvironment.FLAG_DROP, true);

		}
	}

	/**
	 * This method makes the agent turn left.  It does this by updating
	 * the direction of the agent and subtracts the amount of
	 * energy it took to turn.
	 */
	public void turnLeft() {
		position = environment.getTurnLeftPosition(position);
		energy -= params.turnLeftEnergy;
		setWasteCounterLoss(getWasteCounterLoss() - params.turnLeftEnergy);
		stats.addTurn(params.turnLeftEnergy);
		afterTurnAction();
	}

	/**
	 * This method makes the agent turn right.  It does this by updating
	 * the direction of the agent subtracts the amount of energy it took
	 * to turn.
	 */
	public void turnRight() {
		position = environment.getTurnRightPosition(position);
		energy -= params.turnRightEnergy;
		setWasteCounterLoss(getWasteCounterLoss() - params.turnRightEnergy);
		stats.addTurn(params.turnRightEnergy);
		afterTurnAction();
	}


	@Override
	public int type() {
		return agentType;
	}

	public void setWasteCounterLoss(int wasteCounterLoss) {
		this.wasteCounterLoss = wasteCounterLoss;
	}


	public int getWasteCounterLoss() {
		return wasteCounterLoss;
	}

}
