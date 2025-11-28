<?php
/**
 * Weather Service - Fetches real weather data from Open-Meteo API
 * Free API, no key required
 * https://open-meteo.com/
 */

class WeatherService {
    // Default location (can be overridden)
    private $latitude = 31.5204;  // Lahore, Pakistan (adjust as needed)
    private $longitude = 74.3587;
    
    private $currentWeatherUrl = 'https://api.open-meteo.com/v1/forecast';
    private $geocodingUrl = 'https://geocoding-api.open-meteo.com/v1/search';
    
    // Weather code to condition mapping
    private $weatherCodes = [
        0 => 'Clear',
        1 => 'Mainly Clear',
        2 => 'Partly Cloudy',
        3 => 'Overcast',
        45 => 'Foggy',
        48 => 'Depositing Rime Fog',
        51 => 'Light Drizzle',
        53 => 'Moderate Drizzle',
        55 => 'Dense Drizzle',
        61 => 'Slight Rain',
        63 => 'Moderate Rain',
        65 => 'Heavy Rain',
        71 => 'Slight Snow',
        73 => 'Moderate Snow',
        75 => 'Heavy Snow',
        77 => 'Snow Grains',
        80 => 'Slight Rain Showers',
        81 => 'Moderate Rain Showers',
        82 => 'Violent Rain Showers',
        85 => 'Slight Snow Showers',
        86 => 'Heavy Snow Showers',
        95 => 'Thunderstorm',
        96 => 'Thunderstorm with Slight Hail',
        99 => 'Thunderstorm with Heavy Hail'
    ];
    
    // Wind direction from degrees
    private function getWindDirection($degrees) {
        $directions = ['N', 'NNE', 'NE', 'ENE', 'E', 'ESE', 'SE', 'SSE', 
                       'S', 'SSW', 'SW', 'WSW', 'W', 'WNW', 'NW', 'NNW'];
        $index = round($degrees / 22.5) % 16;
        return $directions[$index];
    }
    
    /**
     * Set custom location
     */
    public function setLocation($lat, $lon) {
        $this->latitude = $lat;
        $this->longitude = $lon;
    }
    
    /**
     * Geocode a location name + country to lat/lon using Open-Meteo Geocoding API
     */
    public function geocode($location, $country = null) {
        $searchQuery = $country ? "$location, $country" : $location;
        
        $params = http_build_query([
            'name' => $searchQuery,
            'count' => 1,
            'language' => 'en',
            'format' => 'json'
        ]);
        
        $url = $this->geocodingUrl . '?' . $params;
        $data = $this->fetchFromApi($url);
        
        if (!isset($data['results']) || empty($data['results'])) {
            // Return default location if geocoding fails
            return [
                'latitude' => $this->latitude,
                'longitude' => $this->longitude,
                'name' => $location,
                'country' => $country ?? 'Unknown'
            ];
        }
        
        $result = $data['results'][0];
        return [
            'latitude' => $result['latitude'],
            'longitude' => $result['longitude'],
            'name' => $result['name'] ?? $location,
            'country' => $result['country'] ?? $country
        ];
    }

    /**
     * Get weather condition from code
     */
    private function getCondition($code) {
        return $this->weatherCodes[$code] ?? 'Unknown';
    }
    
    /**
     * Make HTTP request to API
     */
    private function fetchFromApi($url) {
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_TIMEOUT, 10);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        
        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $error = curl_error($ch);
        curl_close($ch);
        
        if ($error) {
            throw new Exception("API request failed: " . $error);
        }
        
        if ($httpCode !== 200) {
            throw new Exception("API returned HTTP $httpCode");
        }
        
        return json_decode($response, true);
    }
    
    /**
     * Get current weather
     */
    public function getCurrentWeather($lat = null, $lon = null) {
        $lat = $lat ?? $this->latitude;
        $lon = $lon ?? $this->longitude;
        
        $params = http_build_query([
            'latitude' => $lat,
            'longitude' => $lon,
            'current' => 'temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m,surface_pressure,uv_index',
            'timezone' => 'auto'
        ]);
        
        $url = $this->currentWeatherUrl . '?' . $params;
        $data = $this->fetchFromApi($url);
        
        if (!isset($data['current'])) {
            throw new Exception("Invalid API response");
        }
        
        $current = $data['current'];
        
        return [
            'temperature' => round($current['temperature_2m'], 1),
            'humidity' => (int)$current['relative_humidity_2m'],
            'condition' => $this->getCondition($current['weather_code']),
            'weather_code' => $current['weather_code'],
            'wind_speed' => round($current['wind_speed_10m'], 1),
            'wind_direction' => $this->getWindDirection($current['wind_direction_10m']),
            'pressure' => round($current['surface_pressure'], 1),
            'uv_index' => round($current['uv_index'] ?? 0, 1),
            'recorded_at' => date('Y-m-d H:i:s'),
            'location' => [
                'latitude' => $lat,
                'longitude' => $lon,
                'timezone' => $data['timezone'] ?? 'UTC'
            ]
        ];
    }
    
    /**
     * Get weather forecast (up to 16 days)
     */
    public function getForecast($days = 7, $lat = null, $lon = null) {
        $lat = $lat ?? $this->latitude;
        $lon = $lon ?? $this->longitude;
        $days = min(max($days, 1), 16); // Clamp between 1-16
        
        $params = http_build_query([
            'latitude' => $lat,
            'longitude' => $lon,
            'daily' => 'temperature_2m_max,temperature_2m_min,weather_code,precipitation_probability_max,relative_humidity_2m_max,wind_speed_10m_max',
            'timezone' => 'auto',
            'forecast_days' => $days
        ]);
        
        $url = $this->currentWeatherUrl . '?' . $params;
        $data = $this->fetchFromApi($url);
        
        if (!isset($data['daily'])) {
            throw new Exception("Invalid API response");
        }
        
        $daily = $data['daily'];
        $forecast = [];
        
        for ($i = 0; $i < count($daily['time']); $i++) {
            $forecast[] = [
                'date' => $daily['time'][$i],
                'temp_high' => round($daily['temperature_2m_max'][$i], 1),
                'temp_low' => round($daily['temperature_2m_min'][$i], 1),
                'condition' => $this->getCondition($daily['weather_code'][$i]),
                'weather_code' => $daily['weather_code'][$i],
                'precipitation_chance' => (int)$daily['precipitation_probability_max'][$i],
                'humidity' => (int)$daily['relative_humidity_2m_max'][$i],
                'wind_speed' => round($daily['wind_speed_10m_max'][$i], 1)
            ];
        }
        
        return [
            'forecast' => $forecast,
            'location' => [
                'latitude' => $lat,
                'longitude' => $lon,
                'timezone' => $data['timezone'] ?? 'UTC'
            ]
        ];
    }
    
    /**
     * Get hourly forecast for the next 24-48 hours
     */
    public function getHourlyForecast($hours = 24, $lat = null, $lon = null) {
        $lat = $lat ?? $this->latitude;
        $lon = $lon ?? $this->longitude;
        
        $params = http_build_query([
            'latitude' => $lat,
            'longitude' => $lon,
            'hourly' => 'temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,precipitation_probability',
            'timezone' => 'auto',
            'forecast_hours' => min($hours, 168)
        ]);
        
        $url = $this->currentWeatherUrl . '?' . $params;
        $data = $this->fetchFromApi($url);
        
        if (!isset($data['hourly'])) {
            throw new Exception("Invalid API response");
        }
        
        $hourly = $data['hourly'];
        $forecast = [];
        $count = min($hours, count($hourly['time']));
        
        for ($i = 0; $i < $count; $i++) {
            $forecast[] = [
                'time' => $hourly['time'][$i],
                'temperature' => round($hourly['temperature_2m'][$i], 1),
                'humidity' => (int)$hourly['relative_humidity_2m'][$i],
                'condition' => $this->getCondition($hourly['weather_code'][$i]),
                'wind_speed' => round($hourly['wind_speed_10m'][$i], 1),
                'precipitation_chance' => (int)$hourly['precipitation_probability'][$i]
            ];
        }
        
        return [
            'hourly' => $forecast,
            'location' => [
                'latitude' => $lat,
                'longitude' => $lon,
                'timezone' => $data['timezone'] ?? 'UTC'
            ]
        ];
    }
}
