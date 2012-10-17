package ru.olamedia.olacraft.world.block;

import java.util.HashMap;

import ru.olamedia.olacraft.world.blockTypes.BlockType;

public class BlockRegistry {

	private HashMap<Integer, String> names = new HashMap<Integer, String>();
	private HashMap<Integer, BlockType> types = new HashMap<Integer, BlockType>();
	private int autoincrement = 0;

	private BlockRegistry worldRegistry;

	public BlockRegistry() {
	}

	public BlockType getBlockType(int id) {
		return types.get(id);
	}

	public String getBlockHumanId(int id) {
		return names.get(id);
	}

	public int registerBlockType(BlockType type) {
		autoincrement++;
		int id = autoincrement;
		String classId = type.getClass().getName();
		names.put(id, classId);
		types.put(id, type);
		return autoincrement;
	}

	public BlockRegistry getWorldRegistry() {
		return worldRegistry;
	}

	public void setWorldRegistry(BlockRegistry worldRegistry) {
		this.worldRegistry = worldRegistry;
	}

}
