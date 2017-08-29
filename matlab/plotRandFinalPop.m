%Takes n random final populations and plots them out to show how final
%populations from two methods differ

n_representatives = 3;
path = '/Users/nozomihitomi/Dropbox/EOSS/problems/climateCentric/result/AIAA JAIS/';
method1 = 'baseline/';
method2 = 'aos/';
method3 = 'all/';
method4 = 'random/';

files1 = dir(strcat(path,method1,'*.obj'));
ind1 = randi(length(files1),n_representatives);

files2 = dir(strcat(path,method2,'*.obj'));
ind2 = randi(length(files2),n_representatives);

% files3 = dir(strcat(path,method3,'*.obj'));
% ind3 = randi(length(files3),n_representatives);
% 
files4 = dir(strcat(path,method4,'*.obj'));
ind4 = randi(length(files4),n_representatives);

%load refPop to show Pareto front
load(strcat(path,filesep,'analysis',filesep,'pop_final',filesep,'refPop.mat'));
[~,ind] = sort(objectives(:,1));

 colors = {
    [0         0.4470    0.7410]
    [0.8500    0.3250    0.0980]
    [0.9290    0.6940    0.1250]
    [0.4940    0.1840    0.5560]
    [0.4660    0.6740    0.1880]
    [0.3010    0.7450    0.9330]
    [0.6350    0.0780    0.1840]};

markers = {'o','+','s','>'};

figure(1)
for i=1:n_representatives
    subplot(1,n_representatives,i);
    pop1 = csvread(strcat(path,method1,files1(ind1(i)).name));
    pop2 = csvread(strcat(path,method2,files2(ind2(i)).name));
%     pop3 = csvread(strcat(path,method3,files3(ind3(i)).name));
    pop4 = csvread(strcat(path,method4,files4(ind4(i)).name));
    scatter(-pop1(:,1),pop1(:,2),20,colors{1}, markers{1})
    hold on
    scatter(-pop2(:,1),pop2(:,2),20,colors{2}, markers{2})
%     scatter(-pop3(:,1),pop3(:,2),20,colors{3}, markers{3})
    scatter(-pop4(:,1),pop4(:,2),20,colors{4}, markers{4})
    plot(-objectives(ind,1), objectives(ind,2),'--k');
    hold off
    xlabel('Scientific Benefit')
    axis([0,0.35,0,4000])
    set(gca,'FontSize',16);
end


subplot(1,n_representatives,1)
legend( '\epsilonMOEA','O-AOS','C-DNF', 'C-ACH', 'PF^*','location','northwest')
ylabel('Lifecycle cost ($FY10M)')
