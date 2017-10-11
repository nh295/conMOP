function [x,y,z] = orbitPlot(s,e,i,r,p,a)
%simple, static plot of orbits based on classical orbital elements
%Inputs:
% s = semi-major axis [m]
% e = eccentricity [0,1]
% i = inclination [rad]
% r = right ascension of ascending node [rad]
% p = argument of perigee [rad]
% a = true anomaly [rad]
%cd
%Output
% x = x coordinates
% y = y coordinates
% z = z coordinates


%plot earth
[eX,eY,eZ] = sphere(20);
re = 6371900;
% surf(eX*6371900,eY*6371900,eZ*6371900,'FaceColor','flat');
cla
hold on
colors = colormap('jet');
edges = linspace(0,deg2rad(120),65);
color_ind = zeros(length(i),1);
for j=1:length(s)
    for k=1:length(edges)-1
        if and(i(j) > edges(k),i(j) < edges(k+1))
            color_ind(j) = k;
        end
    end
end

for sat_i = 1 : length(s)
    
    %create an ellipse with focus at [0,0,0] in x-y plane with +x direction as
    %ascending node and argument of perigee.
    c = e(sat_i)*s(sat_i);
    x1 = -(s(sat_i)-c); x2 = s(sat_i)+c;
    y1 = 0; y2 = 0;
    ea = 1/2*sqrt((x2-x1)^2+(y2-y1)^2);
    eb = ea*sqrt(1-e(sat_i)^2);
    t = linspace(0,2*pi);
    X = ea*cos(t);
    Y = eb*sin(t);
    w = atan2(y2-y1,x2-x1);
    x = (x1+x2)/2 + X*cos(w) - Y*sin(w);
    y = (y1+y2)/2 + X*sin(w) + Y*cos(w);
    z = zeros(1,length(t));
    pts = [x;y;z];
    satPt = [ea*cos(a(sat_i));eb*sin(a(sat_i));0];
    
    %rotate about y for inclination
    R_inc = rotx(rad2deg(i(sat_i)));
    R_raan = rotz(rad2deg(r(sat_i)));
    satPt = R_raan*R_inc*satPt;
    for j=1:length(t)
        pts(:,j) = R_raan*R_inc*pts(:,j);
    end
    plot3(pts(1,:),pts(2,:),pts(3,:),'Color',colors(color_ind(sat_i),:), 'LineWidth',2)
    scatter3(satPt(1,:),satPt(2,:),satPt(3,:),200,colors(color_ind(sat_i),:),'filled')
end

hold off
lim = re + 1000000;
axis([-lim,lim,-lim,lim,-lim,lim]);
axis square
xlabel('x')
ylabel('y')
zlabel('z')
h = colorbar;
ylabel(h,'inclnation [deg]');
view(3)
h.Ticks = [0:12]/12;
h.TickLabels = {0,10,20,30,40,50,60,70,80,90,100,110,120};