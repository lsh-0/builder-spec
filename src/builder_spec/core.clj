(ns builder-spec.core
  (:require
   [clojure.tools.namespace.repl :as tn :refer [refresh]]
   [orchestra.core :refer [defn-spec]]
   [orchestra.spec.test :as st]
   [clojure.spec.alpha :as s]
   [clj-yaml.core :as yaml]))

(defn-spec str-uri? boolean?
  [string string?]
  (try
    (uri? (java.net.URI. string))
    (catch java.net.URISyntaxException urie
      false)))

(defn-spec git-uri? boolean?
  [string string?]
  (str-uri? (subs string 4))) ;; removes "git@" prefix

;; anything that is just 'map?' is a red flag

(s/def ::cidr string?)
(s/def ::ip-address string?)
(s/def ::az string?)

;; top level
(s/def ::salt string?)
(s/def ::description string?)
(s/def ::configuration-repo git-uri?)
(s/def ::gcp-alt map?) 
(s/def ::aws-alt map?)

;; vagrant
(s/def ::box string?)
(s/def ::ip ::ip-address)
(s/def ::ram pos-int?)
(s/def ::cpus pos-int?)
(s/def ::cpucap pos-int?)
(s/def :vagrant/ports map?)

;; aws
(s/def ::subnet-id string?)
(s/def ::redundant-availability-zone ::az)
(s/def ::redundant-subnet-id string?)
(s/def ::redundant-subnet-cidr ::cidr)
(s/def ::vpc-id string?)
(s/def ::region string?)
(s/def :aws/ports (s/coll-of (s/or :simple-port pos-int?, :complex-port map?)))
(s/def ::subnet-cidr ::cidr)
(s/def ::sns (s/coll-of string?))
(s/def ::account_id int?)
(s/def ::subdomains (s/coll-of string?))
(s/def ::sqs map?)
(s/def ::type string?)
(s/def ::availability-zone ::az)
(s/def ::s3 map?)

;; ec2
(s/def ::cluster-size pos-int?)
(s/def ::dns-external-primary boolean?)
(s/def ::dns-internal boolean?)
(s/def ::overrides map?)
(s/def ::suppressed (s/or :nothing-suppressed empty?, :suppressed (s/coll-of pos-int?)))
(s/def ::ami string?)
(s/def ::masterless boolean?)
(s/def ::master_ip ::ip-address)
(s/def ::security-group map?)


;; vault

(s/def ::address str-uri?)

;;

(s/def ::vault (s/keys :req-un [::address]))

(s/def ::ec2 (s/keys :req-un [::cluster-size ::dns-external-primary ::dns-internal ::overrides ::suppressed ::ami ::masterless ::master_ip ::security-group]))

(s/def ::aws (s/keys :req-un [::vault ::ec2
                              ::subnet-id ::redundant-availability-zone ::redundant-subnet-id ::redundant-subnet-cidr ::vpc-id ::region :aws/ports ::subnet-cidr
                              ::sns ::account_id ::subdomains ::sqs ::type ::availability-zone ::s3]))
(s/def ::vagrant (s/keys :req-un [::box ::ip ::ram ::cpus ::cpucap :vagrant/ports]))
(s/def ::project-config (s/keys :req-un [::salt ::description ::configuration-repo ::gcp-alt ::aws ::aws-alt ::vagrant]))

;;

(defn-spec read-project-config ::project-config
  []
  (yaml/parse-string (slurp "resources/lax.yaml")))

(st/instrument)
