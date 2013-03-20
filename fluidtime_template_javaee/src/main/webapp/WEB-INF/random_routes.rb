require 'rubystats'

def distance_factor(x)	
	x = 1-(14-x)/14
	puts x
	return x
end

def emissions(y, mot_id) #y is the duration with and mot, mot_id is the id of the mode of transport
	emissions_factor = [0, 0, 3, 1, 0.5, 0.5]
	return y*emissions_factor[mot_id]
end

def generate_routes(how_many = 1) #how many routes to generate

	#distance = [1,2,3,4,5,6,7,8,9,10,11,12,13,14]
	mot = ["walk", "bicycle", "car", "bus", "metro", "tram"]
	mot_mean_time = [20, 20, 30, 25, 30, 30]
	mot_sd_time = [10, 10, 10, 5, 10, 10]
	park_and_ride = ["yes", "no"] #not used for now

	distance = 0

	gen = Rubystats::NormalDistribution.new(7, 5)
	while distance < 1 do
		distance = gen.rng
	end
	
	routes = Array.new
	while how_many > 0 do
		number_of_mot = 0 # defines the number of transportation means to be used in this trip

		gen = Rubystats::NormalDistribution.new(1, 2)
		while number_of_mot < 1 do
			number_of_mot = gen.rng
		end

		puts "distance: " + distance.to_s
		puts "number_of_mot: " + number_of_mot.round.to_s

		modes_of_transport = Array.new
		duration_of_transport = Array.new
		emissions_of_transport = Array.new
		i=0
		while i < number_of_mot do
			tmp_mot = mot[rand(mot.length)]
			if (not modes_of_transport.include? tmp_mot)
				modes_of_transport.push(tmp_mot)
				gen = Rubystats::NormalDistribution.new(distance_factor(distance)*mot_mean_time[mot.index(tmp_mot)], distance_factor(distance)*mot_sd_time[mot.index(tmp_mot)])
				duration = 0
				while duration < 1
					duration = gen.rng
				end
				duration_of_transport.push(duration)
				emissions_of_transport.push(emissions(duration, mot.index(tmp_mot)))
				i = i + 1
			end
		end

		i=0
		trip = Array.new
		while i < modes_of_transport.length do
			segment = Hash.new
			puts i.to_s + " mode of transport: " + modes_of_transport[i] + 
				" duration: " + duration_of_transport[i].to_s +
				" emissions: " + emissions_of_transport[i].to_s			
			segment["mot"] = modes_of_transport[i]
			segment["duration"] = duration_of_transport[i]
			segment["emissions"] = emissions_of_transport[i]
			trip.push(segment)
			i = i + 1
		end
		routes.push(trip)
		
		how_many = how_many - 1
	end
	
	return routes
end

def getcontext
	weather = ["sunny", "rainy", "cloudy"]
	temperature = ["hot", "warm", "normal", "cold", "freezing"]
	#context parameters
	weather = weather[rand(weather.length)]
	temperature = temperature[rand(temperature.length)]
	
	context = Hash.new
	context["weather"] = weather
	context["temperature"] = temperature
	
	puts "Context Description: " 
	puts "	weather: " + weather
	puts "	temperature: " + temperature
	return context
end

generate_routes 5