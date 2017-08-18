local os = require "cfg.structs"

local insert = table.insert
local concat = table.concat
local tostring = tostring

local function dump_atom (x)
    return tostring(x)
end

local function dump_table(t)
  local code = {"{"}
  
  for k, v in pairs(t) do
    if type(v) ~= "table" then
      insert(code, tostring(k) .. "=" .. dump_atom(v) .. ",")
    else
      insert(code, tostring(k) .. "=" .. dump_table(v) .. ",")
    end
  end
 insert(code, "}")
 return concat(code)
end



local os = require "cfg.beans"

os.lines = function(datafile) return io.lines("data/" .. datafile) end






local user  = {
	'id' = 1,
	'name' = 2,
}
user.__index = function (t, key)
	rawget(t, user[key])
end

user.__newindex = function(t, key, value)
	rawset(t, user[key], value)
end

user[
