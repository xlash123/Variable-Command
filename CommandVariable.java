package net.minecraft.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

public class CommandVariable extends CommandBase{

	private List<String> specialValues = new ArrayList<String>();
	
	public CommandVariable(){
		//Get the scoreboard value (entity,objective)
		this.specialValues.add("Score.getScore(");
		//Resolve an entity selector by converting to a name. (@p) or (@e), etc.
		this.specialValues.add("Entity.resolve(");
		//Gets the NBT data from an Entity. (entity,tag).
		this.specialValues.add("Entity.nbt(");
		//Runs the following command and gives a value of the entity specified only if the NBT data matches.
		//(entity,{data tag})
		this.specialValues.add("Entity.checkNbt(");
		//Generates a random integer from the between the 1st and 2nd argument, inclusive.
		this.specialValues.add("Random.int(");
	}
	
	/**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
	
	@Override
	public String getCommandName() {
		return "variable";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/variable <name> <value> <command>";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if(args.length < 3){
			throw new WrongUsageException(this.getCommandUsage(sender));
		}
		else{
			String value;
			try {
				value = this.processValue(args[1], sender);
			} catch (NBTException e) {
				throw new SyntaxErrorException(e.getMessage());
			}
			String name = args[0];
			for(int i = 0; i < args.length; i++){
				if(args[i].contains("var:" + name)){
					args[i] = args[i].replaceAll("var:" + name, value);
				}
			}
			
			String var24 = func_180529_a(args, 2);
			ICommandManager var25 = MinecraftServer.getServer().getCommandManager();

            try
            {
                int var16 = var25.executeCommand(sender, var24);

                if (var16 < 1)
                {
                    throw new CommandException("commands.execute.allInvocationsFailed", new Object[] {var24});
                }
            }
            catch (Throwable var23)
            {
                throw new CommandException("commands.execute.failed", new Object[] {var24, sender.getName()});
            }
		}
	}
	
	public String processValue(String s, ICommandSender sender) throws CommandException, NBTException{
		String start = s;
		for(int i = 0; i < specialValues.size(); i++){
			if(s.startsWith(specialValues.get(i)) && s.endsWith(")")){
				s = s.replace(specialValues.get(i), "");
				s = s.substring(0, s.length()-1);
				String[] split = s.split(",");
				if(start.startsWith(specialValues.get(0))){
					return "" + this.getScoreboard().getValueFromObjective(func_175758_e(sender, split[0]), this.getScoreboard().getObjective(split[1])).getScorePoints();
				}else if(start.startsWith(specialValues.get(1))){
					return func_175758_e(sender, split[0]);
				}else if(start.startsWith(specialValues.get(2))){
					NBTTagCompound nbt = new NBTTagCompound();
					Entity entity = func_175768_b(sender, split[0]);
					entity.writeToNBT(nbt);
					return nbt.getTag(split[1]).toString();
				}else if(start.startsWith(specialValues.get(3))){
					String compound = "";
					for(int ii = 1; ii < split.length; ii++){
						compound = compound + split[ii];
						if(ii < split.length-1) compound = compound + ",";
					}
					System.out.println(compound);
					NBTTagCompound nbt = JsonToNBT.func_180713_a(compound);
					Entity entity = func_175768_b(sender, split[0]);
					NBTTagCompound eTag = new NBTTagCompound();
					entity.writeToNBT(eTag);
					NBTTagCompound pre = new NBTTagCompound();
					entity.writeToNBT(pre);
					eTag.merge(nbt);
					if(eTag.equals(pre)){
						return func_175758_e(sender, split[0]);
					}
					else throw new CommandException("The NBT tag does not match up with the specified entity.");
				}else if(start.startsWith(specialValues.get(4))){
					try{
						int r = new Random().nextInt(Integer.parseInt(split[1])+1-Integer.parseInt(split[0])) + Integer.parseInt(split[0]);
						return "" + r;
					}catch(NumberFormatException e){
						throw new SyntaxErrorException("The parameters must be in number form.");
					}
				}
			}
		}
		return s;
	}
	
	protected Scoreboard getScoreboard()
    {
        return MinecraftServer.getServer().worldServerForDimension(0).getScoreboard();
    }
	
	public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 2)
        {
            List var4 = this.specialValues;
            return getListOfStringsMatchingLastWord(args, (String[])var4.toArray(new String[var4.size()]));
        }
        else
        {
            return null;
        }
    }

}
