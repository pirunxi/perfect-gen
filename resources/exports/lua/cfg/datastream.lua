 local tonumber = tonumber
 local sub = string.sub
 local gsub = string.gsub
 local gmatch = string.gmatch
 local lower = string.lower
 local error = error
 local open = io.open
 local setmetatable = setmetatable
 local tostring = tostring
 local insert = table.insert
 
local find = string.find
local function split(s)
	local fs = {}
	local p = 1
	while true do
		local b, e = find(s, ",", p, true)
		if b then
			insert(fs, sub(s, p, b - 1))
		else
			insert(fs, sub(s, p, -1))
			return fs
		end
		p = e + 1
	end
end
 
 local os = {}
 os.__index = os
 
 function os.new(line)
    local o = {datas = split(line), index=1}
    setmetatable(o, os)
	--o.data_iter = io.lines(datafile)
    return o
 end
 
 --[[
 
 os.lines = function(datafile) return os.lines(datafile) end
 --]]
 
 function os:dump()
    print(table.concat(self.datas, ","))
 end
 
 
 function os:get_next()
	local index = self.index
	self.index = index + 1
    return self.datas[index]
 end
 
function os:get_bool()
    local next = lower(self:get_next())
    if next == "true" then
        return true
    elseif next == "false" then
        return false
    else
        error(tostring(next) .. "isn't bool")
    end
 end
 
function os:get_int()
    local next = self:get_next()
    return tonumber(next)
 end
 
function os:get_long() 
    local next = self:get_next()
    return tonumber(next)
 end 
 
function os:get_float()
    local next = self:get_next()
    return tonumber(next)
 end
 
function os:get_string() 
	local next = self:get_next()
	return gsub(gsub(next, "%$enter%$", "\n"), "%$comma%$", ",")
end 
function os:get_list(key)
    local r = {}
    local oper = self["get_" .. key]
    for i = 1, self:get_int() do
		local v = oper(self)
		insert(r, v)
        --  insert(r, oper(self))
    end
    return r
 end 
 
function os:get_set(key)
    local r = {}
    local oper = self["get_" .. key]
    for i = 1, self:get_int() do
        r[oper(self)] = true
    end
    return r
 end 
 
function os:get_map(key, value)
    local r = {}
    local oper_key = self["get_" .. key]
    local oper_value = self["get_" .. value]
    for i = 1, self:get_int() do
        r[oper_key(self)] = oper_value(self)
    end
    return r
 end
 
 return os