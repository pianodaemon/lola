ECHO = /bin/echo

BASE		:= $(IMPT_ROOT)
OPERATIONAL	:= $(BASE)/DOS
REGISTRY	:= localhost:5000

# Inhouse images for local registry
SSO_IMG        := $(REGISTRY)/lola/sso:latest
CFDI_IMG       := $(REGISTRY)/lola/cfdi:latest

all:	sso_push cfdi_push

sso_push:	sso_build
	(docker push $(SSO_IMG))

cfdi_push:	cfdi_build
	(docker push $(CFDI_IMG))

sso_build:
	(cd $(OPERATIONAL)/sso && \
	docker image build -f Dockerfile . \
	-t "$(SSO_IMG)")

cfdi_build:
	(cd $(OPERATIONAL)/cfdi && \
	docker image build -f Dockerfile . \
	-t "$(CFDI_IMG)")

clean:
	- (docker rmi $(SSO_IMG))
	- (docker rmi $(CFDI_IMG))
