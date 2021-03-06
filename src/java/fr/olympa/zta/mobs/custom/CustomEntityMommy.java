package fr.olympa.zta.mobs.custom;

import java.sql.SQLException;
import java.util.Arrays;

import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers.NBT;
import org.bukkit.metadata.FixedMetadataValue;

import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;
import net.minecraft.server.v1_16_R3.AttributeProvider;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.GenericAttributes;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;
import net.minecraft.server.v1_16_R3.World;

public class CustomEntityMommy extends CustomEntityZombie { // ! it's a husk !
	
	public final int MOMMY_DIE_TICKS = 7 * 60 * 20;
	
	private int dieTime = MOMMY_DIE_TICKS;
	//private int lastTick = MinecraftServer.currentTick;
	
	private String player;
	private ItemStack[] contents;
	private boolean contentsLoaded = false;
	
	public CustomEntityMommy(EntityTypes<CustomEntityMommy> type, World world) {
		super(type, world);
		setZombieType(Zombies.COMMON, true);
	}
	
	public static AttributeProvider.Builder getAttributeBuilder() {
		return CustomEntityZombie.getAttributeBuilder().a(GenericAttributes.MAX_HEALTH, 40).a(GenericAttributes.MOVEMENT_SPEED, 0.32).a(GenericAttributes.ATTACK_DAMAGE, 9).a(GenericAttributes.ARMOR, 2.4);
	}
	
	public void setContents(org.bukkit.inventory.ItemStack[] bukkitItems) {
		contents = Arrays.stream(bukkitItems).filter(x -> x != null).map(CraftItemStack::asNMSCopy).toArray(ItemStack[]::new);
		contentsLoaded = true;
	}
	
	public void setPlayer(String player) {
		this.player = player;
		getBukkitEntity().setMetadata("player", new FixedMetadataValue(OlympaZTA.getInstance(), player));
	}
	
	@Override
	protected int getTargetChance() {
		return 1;
	}
	
	@Override
	protected int getTargetDistance() {
		return 20;
	}
	
	@Override
	public void tick() {
		super.tick();
		if (!killed) {
			/*int elapsedTicks = MinecraftServer.currentTick - this.lastTick; TROP DE RISQUE DE PERTE DE STUFF
			this.lastTick = MinecraftServer.currentTick;
			dieTime -= elapsedTicks;*/
			dieTime--;
			if (dieTime < 0) this.killEntity();
		}
	}
	
	@Override
	protected void dropDeathLoot(DamageSource damagesource, int i, boolean flag) {
		if (contents != null) {
			OlympaZTA.getInstance().sendMessage("%d items dropp??s depuis un zombie momifi??.", contents.length);
			if (!contentsLoaded) {
				OlympaZTA.getInstance().getTask().runTaskAsynchronously(() -> {
					try {
						OlympaZTA.getInstance().gunRegistry.loadFromItems(Arrays.stream(contents).map(CraftItemStack::asCraftMirror).toArray(org.bukkit.inventory.ItemStack[]::new));
					}catch (SQLException e) {
						OlympaZTA.getInstance().sendMessage("??cUne erreur est survenue lors du chargement des armes sur un zombie.");
						e.printStackTrace();
					}
				});
			}
			for (ItemStack item : contents) {
				a(item);
			}
		}else OlympaZTA.getInstance().sendMessage("??cPas de contenu dans l'inventaire d'un zombie momifi??.");
	}
	
	@Override
	protected boolean eN() { // convertsInWater
		return false;
	}
	
	@Override
	protected boolean T_() { // isSunSensitive
		return false;
	}
	
	@Override
	public boolean isPersistent() { // requiresCustomPersistence
		return true;
	}
	
	@Override
	public void saveData(NBTTagCompound nbttagcompound) {
		super.saveData(nbttagcompound);
		nbttagcompound.setInt("MommyDieTime", this.dieTime);
		
		NBTTagList nbtList = new NBTTagList();
		for (ItemStack item : contents) {
			if (item != null) {
				NBTTagCompound nbtItem = new NBTTagCompound();
				item.save(nbtItem);
				nbtList.add(nbtItem);
			}
		}
		nbttagcompound.set("PlayerInventory", nbtList);
		if (player != null) nbttagcompound.setString("PlayerDead", player);
	}
	
	@Override
	public void loadData(NBTTagCompound nbttagcompound) {
		super.loadData(nbttagcompound);
		if (nbttagcompound.hasKey("MommyDieTime")) this.dieTime = nbttagcompound.getInt("MommyDieTime");
		
		if (nbttagcompound.hasKey("PlayerInventory")) {
			NBTTagList nbtList = nbttagcompound.getList("PlayerInventory", NBT.TAG_COMPOUND);
			contents = nbtList.stream().map(x -> ItemStack.a((NBTTagCompound) x)).toArray(ItemStack[]::new);
			if (contents.length != 0) OlympaZTA.getInstance().sendMessage("%d contenus charg??s depuis les tags NBT d'un zombie.", contents.length);
		}
		
		if (nbttagcompound.hasKey("PlayerDead")) setPlayer(nbttagcompound.getString("PlayerDead"));
	}
	
}
