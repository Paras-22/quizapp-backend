// src/components/ui/StatsCard.js
import React from 'react';
import Card from './Card';

const StatsCard = ({ title, value, icon, color = 'blue', trend = null }) => (
  <Card className={`border-l-4 border-${color}-500`} hover>
    <div className="flex items-center justify-between">
      <div className="flex items-center">
        <div className={`flex-shrink-0 p-3 bg-${color}-100 rounded-lg`}>
          {icon}
        </div>
        <div className="ml-4">
          <p className="text-sm font-medium text-gray-600">{title}</p>
          <p className="text-2xl font-semibold text-gray-900">{value}</p>
          {trend && (
            <p className={`text-sm ${trend.positive ? 'text-green-600' : 'text-red-600'}`}>
              {trend.value}
            </p>
          )}
        </div>
      </div>
    </div>
  </Card>
);

export default StatsCard;